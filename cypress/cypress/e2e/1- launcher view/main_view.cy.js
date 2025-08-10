describe("example to-do app", () => {
  beforeEach(() => {
    cy.visit("http://localhost:1420");
  });
  it("should load.", () => {
    cy.get("#play-now")
      .invoke("text")
      .then((text) => {
        expect(text.trim()).to.equal("Jugar Ahora");
      });
  });
});
