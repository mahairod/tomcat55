/* Generated By:JJTree&JavaCC: Do not edit this line. HttpParser.java */
package org.apache.tomcat.util.http.parser;
@SuppressWarnings("all") // Ignore warnings in generated code
public class HttpParser/*@bgen(jjtree)*/implements HttpParserTreeConstants, HttpParserConstants {/*@bgen(jjtree)*/
  protected JJTHttpParserState jjtree = new JJTHttpParserState();

/*
 * Content-Type
 */
  final public AstMediaType MediaType() throws ParseException {
                                       /*@bgen(jjtree) MediaType */
  AstMediaType jjtn000 = new AstMediaType(JJTMEDIATYPE);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      Type();
      jj_consume_token(FORWARD_SLASH);
      SubType();
      label_1:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case SEMI_COLON:
          ;
          break;
        default:
          jj_la1[0] = jj_gen;
          break label_1;
        }
        jj_consume_token(SEMI_COLON);
        Parameter();
      }
      jj_consume_token(0);
                                                                          jjtree.closeNodeScope(jjtn000, true);
                                                                          jjtc000 = false;
        {if (true) return jjtn000;}
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public void Type() throws ParseException {
                     /*@bgen(jjtree) Type */
                      AstType jjtn000 = new AstType(JJTTYPE);
                      boolean jjtc000 = true;
                      jjtree.openNodeScope(jjtn000);Token t = null;
    try {
      t = jj_consume_token(HTTP_TOKEN);
                     jjtree.closeNodeScope(jjtn000, true);
                     jjtc000 = false;
                     jjtn000.jjtSetValue(t.image.trim());
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  final public void SubType() throws ParseException {
                          /*@bgen(jjtree) SubType */
                           AstSubType jjtn000 = new AstSubType(JJTSUBTYPE);
                           boolean jjtc000 = true;
                           jjtree.openNodeScope(jjtn000);Token t = null;
    try {
      t = jj_consume_token(HTTP_TOKEN);
                     jjtree.closeNodeScope(jjtn000, true);
                     jjtc000 = false;
                     jjtn000.jjtSetValue(t.image.trim());
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  final public void Parameter() throws ParseException {
                               /*@bgen(jjtree) Parameter */
  AstParameter jjtn000 = new AstParameter(JJTPARAMETER);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      Attribute();
      jj_consume_token(EQUALS);
      Value();
    } catch (Throwable jjte000) {
      if (jjtc000) {
        jjtree.clearNodeScope(jjtn000);
        jjtc000 = false;
      } else {
        jjtree.popNode();
      }
      if (jjte000 instanceof RuntimeException) {
        {if (true) throw (RuntimeException)jjte000;}
      }
      if (jjte000 instanceof ParseException) {
        {if (true) throw (ParseException)jjte000;}
      }
      {if (true) throw (Error)jjte000;}
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  final public void Attribute() throws ParseException {
                                /*@bgen(jjtree) Attribute */
                                 AstAttribute jjtn000 = new AstAttribute(JJTATTRIBUTE);
                                 boolean jjtc000 = true;
                                 jjtree.openNodeScope(jjtn000);Token t = null;
    try {
      t = jj_consume_token(HTTP_TOKEN);
                     jjtree.closeNodeScope(jjtn000, true);
                     jjtc000 = false;
                     jjtn000.jjtSetValue(t.image.trim());
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  final public void Value() throws ParseException {
                       /*@bgen(jjtree) Value */
                        AstValue jjtn000 = new AstValue(JJTVALUE);
                        boolean jjtc000 = true;
                        jjtree.openNodeScope(jjtn000);Token t = null;
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case HTTP_TOKEN:
        t = jj_consume_token(HTTP_TOKEN);
                     jjtree.closeNodeScope(jjtn000, true);
                     jjtc000 = false;
                     jjtn000.jjtSetValue(t.image.trim());
        break;
      case QUOTED_STRING:
        t = jj_consume_token(QUOTED_STRING);
                                                                                  jjtree.closeNodeScope(jjtn000, true);
                                                                                  jjtc000 = false;
                                                                                  jjtn000.jjtSetValue(t.image.trim());
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } finally {
      if (jjtc000) {
        jjtree.closeNodeScope(jjtn000, true);
      }
    }
  }

  /** Generated Token Manager. */
  public HttpParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[2];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x2,0x180,};
   }

  /** Constructor with InputStream. */
  public HttpParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public HttpParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new HttpParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public HttpParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new HttpParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public HttpParser(HttpParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(HttpParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 2; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[14];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 2; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 14; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
