/**
 * Singleton class represents a Lexical Token in JACK
 */

class Token {

    public enum TokenType { 
        KEYWORD {
            public String toString() {
                return "keyword";
            }
        },
        SYMBOL {
            public String toString() {
                return "symbol";
            }

        },
        IDENTIFIER{
            public String toString() {
                return "identifier";
            }

        },
        INT_CONST{
            public String toString() {
                return "integerConstant";
            }

        },
        STRING_CONST {
            public String toString() {
                return "stringConstant";
            }

        }
    };

    private TokenType type;

    private String value;

    private static Token instance;

    private Token() {}

    /**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the type
	 */
	public TokenType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(TokenType type) {
		this.type = type;
    }

    

	private Token set(TokenType type, String value) {
        this.setType(type);
        this.setValue(value);

        return this;
    }

    private static Token getInstance() {
        if(null == instance) {
            instance = new Token();
        }
        return instance;
    }

    public static Token Keyword(String value) {
        return getInstance().set(TokenType.KEYWORD, value);
    }

    public static Token Symbol(String value) {
        return getInstance().set(TokenType.SYMBOL, value);
    }

    public static Token Int_Const(String value) {
        return getInstance().set(TokenType.INT_CONST, value);
    }


    public static Token String_Const(String value) {
        return getInstance().set(TokenType.STRING_CONST, value);
    }

    public static Token Identifier(String value) {
        return getInstance().set(TokenType.IDENTIFIER, value);
    }

}