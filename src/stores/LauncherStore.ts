import { defineStore } from "pinia";
import { ref, computed, readonly } from "vue";
import { z } from "zod/v4";

// Esquemas de validación con Zod
const LoaderSchema = z.object({
  loader: z.string().min(1, "Loader name is required"),
  version: z.string().min(1, "Loader version is required"),
});

const GameSchema = z.object({
  version: z.string().min(1, "Game version is required"),
});

export const InstanceSchema = z.object({
  name: z
    .string()
    .min(1, "Instance name is required")
    .max(50, "Instance name too long"),
  loader: LoaderSchema,
  game: GameSchema,
  lastPlayed: z.string().datetime().optional(),
});

export type Instance = z.infer<typeof InstanceSchema>;

// Datos de ejemplo
const FAKE_INSTANCES: Instance[] = [
  {
    name: "Vanilla 1.20.4",
    loader: { loader: "Vanilla", version: "1.20.4" },
    game: { version: "1.20.4" },
    lastPlayed: "2024-01-15T10:30:00.000Z",
  },
  {
    name: "Fabric Modded",
    loader: { loader: "Fabric", version: "0.15.3" },
    game: { version: "1.20.1" },
    lastPlayed: "2024-01-10T14:45:00.000Z",
  },
  {
    name: "Forge Adventure",
    loader: { loader: "Forge", version: "47.2.20" },
    game: { version: "1.20.1" },
    lastPlayed: "2023-12-28T16:20:00.000Z",
  },
  {
    name: "Quilt Experimental",
    loader: { loader: "Quilt", version: "0.21.2" },
    game: { version: "1.20.2" },
    lastPlayed: "2024-01-05T09:15:00.000Z",
  },
];

// Utilidad para parsing seguro de fechas
const safeParseDate = (dateString?: string): Date => {
  if (!dateString) return new Date(0);
  const date = new Date(dateString);
  return isNaN(date.getTime()) ? new Date(0) : date;
};

// Utilidad para formatear errores de Zod de forma segura
const formatZodErrors = (zodError: z.ZodError): string => {
  try {
    // Usar la propiedad 'issues' que es la correcta en Zod
    if (!zodError.issues || !Array.isArray(zodError.issues)) {
      return "Error de validación";
    }

    return zodError.issues
      .map((issue) => {
        const path =
          issue.path && issue.path.length > 0
            ? `${issue.path.join(".")}: `
            : "";
        return `${path}${issue.message || "Error desconocido"}`;
      })
      .join(", ");
  } catch {
    return "Error de validación";
  }
};

// API real (reemplazar con llamadas reales)
const api = {
  async saveInstance(instance: Instance): Promise<void> {
    const validatedInstance = InstanceSchema.parse(instance);
  },

  async loadInstances(): Promise<Instance[]> {
    return z.array(InstanceSchema).parse(FAKE_INSTANCES);
  },

  async deleteInstance(instanceName: string): Promise<void> {
    z.string().min(1).parse(instanceName);
  },
};

export const useLauncherStore = defineStore("launcher", () => {
  // State
  const currentInstance = ref<Instance | null>(null);
  const instances = ref<Instance[]>([]);
  const isSettingsModalOpen = ref(false);
  const isAddInstanceModalOpen = ref(false);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  // Getters
  const sortedInstances = computed(() =>
    [...instances.value].sort((a, b) => {
      const dateA = safeParseDate(a.lastPlayed).getTime();
      const dateB = safeParseDate(b.lastPlayed).getTime();
      return dateB - dateA;
    }),
  );

  const instanceCount = computed(() => instances.value.length);

  const hasInstances = computed(() => instanceCount.value > 0);

  const getInstanceByName = computed(
    () => (name: string) =>
      instances.value.find((instance) => instance.name === name),
  );

  const recentInstances = computed(() => sortedInstances.value.slice(0, 5));

  // Actions
  const clearError = (): void => {
    error.value = null;
  };

  const setError = (errorMessage: string): void => {
    error.value = errorMessage;
  };

  const addInstance = async (
    instanceData: unknown,
  ): Promise<{ success: boolean; error?: string }> => {
    try {
      clearError();
      isLoading.value = true;

      // Validar datos con Zod
      const validatedInstance = InstanceSchema.parse(instanceData);

      // Validar que no exista una instancia con el mismo nombre
      if (getInstanceByName.value(validatedInstance.name)) {
        throw new Error(
          `Ya existe una instancia con el nombre "${validatedInstance.name}"`,
        );
      }

      // Actualizar lastPlayed a la fecha actual
      const newInstance: Instance = {
        ...validatedInstance,
        lastPlayed: new Date().toISOString(),
      };

      await api.saveInstance(newInstance);
      instances.value.push(newInstance);

      return { success: true };
    } catch (err) {
      if (err instanceof z.ZodError) {
        const errorMessage = `Datos inválidos: ${formatZodErrors(err)}`;
        setError(errorMessage);
        return { success: false, error: errorMessage };
      }

      const errorMessage =
        err instanceof Error
          ? err.message
          : "Error desconocido al añadir instancia";
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      isLoading.value = false;
    }
  };

  const setCurrentInstance = (instance: Instance | null): void => {
    currentInstance.value = instance;
  };

  const updateInstanceLastPlayed = (instanceName: string): void => {
    const instance = getInstanceByName.value(instanceName);
    if (instance) {
      instance.lastPlayed = new Date().toISOString();
    }
  };

  const toggleSettingsModal = (): void => {
    isSettingsModalOpen.value = !isSettingsModalOpen.value;
  };

  const toggleAddInstanceModal = (): void => {
    isAddInstanceModalOpen.value = !isAddInstanceModalOpen.value;
  };

  const loadInstances = async (): Promise<{
    success: boolean;
    error?: string;
  }> => {
    try {
      clearError();
      isLoading.value = true;

      const loadedInstances = await api.loadInstances();

      // Validar todas las instancias cargadas
      const validatedInstances = z.array(InstanceSchema).parse(loadedInstances);
      instances.value = validatedInstances;

      return { success: true };
    } catch (err) {
      if (err instanceof z.ZodError) {
        const errorMessage = `Datos inválidos recibidos: ${formatZodErrors(err)}`;
        setError(errorMessage);
        return { success: false, error: errorMessage };
      }

      const errorMessage =
        err instanceof Error
          ? err.message
          : "Error desconocido al cargar instancias";
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      isLoading.value = false;
    }
  };

  const deleteInstance = async (
    instanceName: string,
  ): Promise<{ success: boolean; error?: string }> => {
    try {
      clearError();
      isLoading.value = true;

      // Validar nombre de instancia
      const validatedName = z
        .string()
        .min(1, "Instance name is required")
        .parse(instanceName);

      await api.deleteInstance(validatedName);

      // Remover de la lista
      instances.value = instances.value.filter(
        (instance) => instance.name !== validatedName,
      );

      // Si era la instancia actual, limpiarla
      if (currentInstance.value?.name === validatedName) {
        currentInstance.value = null;
      }

      return { success: true };
    } catch (err) {
      if (err instanceof z.ZodError) {
        const errorMessage = `Nombre inválido: ${formatZodErrors(err)}`;
        setError(errorMessage);
        return { success: false, error: errorMessage };
      }

      const errorMessage =
        err instanceof Error
          ? err.message
          : "Error desconocido al eliminar instancia";
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      isLoading.value = false;
    }
  };

  const resetToFakeData = (): void => {
    instances.value = [...FAKE_INSTANCES];
    currentInstance.value = null;
    clearError();
  };

  const duplicateInstance = async (
    originalName: string,
    newName: string,
  ): Promise<{ success: boolean; error?: string }> => {
    try {
      // Validar parámetros
      const validatedOriginalName = z.string().min(1).parse(originalName);
      const validatedNewName = z.string().min(1).max(50).parse(newName);

      const original = getInstanceByName.value(validatedOriginalName);
      if (!original) {
        return {
          success: false,
          error: `No se encontró la instancia "${validatedOriginalName}"`,
        };
      }

      const duplicate = {
        ...original,
        name: validatedNewName,
        lastPlayed: new Date().toISOString(),
      };

      return await addInstance(duplicate);
    } catch (err) {
      if (err instanceof z.ZodError) {
        const errorMessage = `Parámetros inválidos: ${formatZodErrors(err)}`;
        setError(errorMessage);
        return { success: false, error: errorMessage };
      }

      const errorMessage =
        err instanceof Error
          ? err.message
          : "Error desconocido al duplicar instancia";
      setError(errorMessage);
      return { success: false, error: errorMessage };
    }
  };

  // Inicialización automática
  const initialize = async (): Promise<void> => {
    if (instances.value.length === 0) {
      await loadInstances();
    }
  };

  // Validar instancia completa
  const validateInstance = (
    data: unknown,
  ): { isValid: boolean; errors?: string[] } => {
    try {
      InstanceSchema.parse(data);
      return { isValid: true };
    } catch (err) {
      if (err instanceof z.ZodError) {
        try {
          const errors =
            err.issues && Array.isArray(err.issues)
              ? err.issues.map((issue) => {
                  const path =
                    issue.path && issue.path.length > 0
                      ? `${issue.path.join(".")}: `
                      : "";
                  return `${path}${issue.message || "Error desconocido"}`;
                })
              : ["Error de validación"];

          return {
            isValid: false,
            errors,
          };
        } catch {
          return { isValid: false, errors: ["Error de validación"] };
        }
      }
      return { isValid: false, errors: ["Error de validación desconocido"] };
    }
  };

  return {
    // State
    currentInstance: readonly(currentInstance),
    instances: readonly(instances),
    isSettingsModalOpen,
    isAddInstanceModalOpen,
    isLoading: readonly(isLoading),
    error: readonly(error),

    // Getters
    sortedInstances,
    instanceCount,
    hasInstances,
    getInstanceByName,
    recentInstances,

    // Actions
    addInstance,
    setCurrentInstance,
    updateInstanceLastPlayed,
    toggleSettingsModal,
    toggleAddInstanceModal,
    loadInstances,
    deleteInstance,
    resetToFakeData,
    duplicateInstance,
    clearError,
    initialize,
    validateInstance,
  };
});

// Esquemas exportables para uso en componentes
export { LoaderSchema, GameSchema };

// Tipos auxiliares
export type LauncherState = "idle" | "loading" | "error";
export type ModalType = "settings" | "addInstance";
