public class Declaration {

    String type;
    String identifier;

    public Declaration(int code, String identifier){
        switch (code){
            case 18:
                this.type = "integer";
                break;
            case 19:
                this.type = "boolean";
                break;
            case 20:
                this.type = "string";
                break;
            case 21:
                this.type = "double";
                break;
            default:
                this.type = "?????????";
        }
        this.identifier = identifier;
    }


}
