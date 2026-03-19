<script>
    /** Color de relleno principal */
    export let color = "#24292e";

    /** Opacidad del relleno (0–1) */
    export let opacity = 1;

    /** Ancho del SVG (cualquier unidad CSS válida) */
    export let width = "1rem";

    /** Alto del SVG (cualquier unidad CSS válida) */
    export let height = "1rem";

    /** Clases CSS adicionales */
    export let className = "";

    /** Atributo aria-label para accesibilidad */
    export let label = "Polyhedron icon";
</script>

<svg
    {width}
    {height}
    viewBox="0 0 512 512"
    version="1.1"
    xmlns="http://www.w3.org/2000/svg"
    role="img"
    aria-label={label}
    class={className}
>
    <g>
        <path
            style="fill:{color};fill-opacity:{opacity}"
            d="m 86.694479,219.56319 -0.628221,126.5865 166.164412,96.43191 h 12.25031 l 164.90798,-99.88712 -59.68098,-32.66749 -0.31411,-118.73374 -101.77178,57.1681 -0.94234,115.90675 56.53988,-34.86626 24.81473,12.25031 -84.49571,56.85399 -137.26626,-73.81596 -0.31411,-81.66871 z"
        />
        <path
            style="fill:{color};fill-opacity:{opacity}"
            d="M 147.03656,307.84393 148.36922,237.21307 85.752147,199.42086 85.678784,143.92703 250.97423,42.404908 h 6.59632 l 172.13252,99.258892 0.31411,164.90798 -39.57792,-21.67362 -0.31411,-114.96442 -134.43926,-76.642942 -125.95828,73.501842 0.0196,10.26748 119.89202,69.86994 -0.23558,117.9092 z"
        />
        <path
            style="fill:{color};fill-opacity:{opacity}"
            d="m 156.42699,174.6454 100.82945,-57.79632 102.4,57.48221 -100.20123,57.48221 z"
        />
    </g>
</svg>
