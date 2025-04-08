package components;

class ComponentFactory {
    public static Component createComponent(String type, String protocol) {
        switch (type.toUpperCase()) {
            case "A":
                return new ComponentA(protocol);
            case "B":
                return new ComponentB(protocol);
            case "C":
                return new ComponentC(protocol);
            default:
                throw new IllegalArgumentException("Tipo de componente inválido: " + type);
        }
    }
}
