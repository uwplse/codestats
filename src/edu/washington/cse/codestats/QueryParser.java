/* Generated By:JavaCC: Do not edit this line. QueryParser.java */
package edu.washington.cse.codestats;

import java.io.*;
import java.util.*;
import edu.washington.cse.codestats.*;

public class QueryParser implements QueryParserConstants {
    static List<Query> parse(InputStream in) throws ParseException, TokenMgrError {
         return new QueryParser(in).program();
    }

  static final public List<Query> program() throws ParseException {
    List<Query> blocks = new ArrayList();
    Query block;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IDENT:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      block = block();
                         blocks.add(block);
    }
    jj_consume_token(0);
                                                        {if (true) return blocks;}
    throw new Error("Missing return statement in function");
  }

  static final public Query block() throws ParseException {
    Token name = null;
    Token deriving = null;
    Metric metric = null;
    QueryTarget target = null;
    PredicateMirror pred = null;
    name = jj_consume_token(IDENT);
    jj_consume_token(23);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case EXISTS:
      jj_consume_token(EXISTS);
                                     metric = Metric.EXISTS;
      break;
    case COUNT:
      jj_consume_token(COUNT);
                                                                           metric = Metric.SUM;
      break;
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case EXPR:
      jj_consume_token(EXPR);
              target = QueryTarget.EXPRESSION;
      break;
    case STMT:
      jj_consume_token(STMT);
                                                            target = QueryTarget.STATEMENT;
      break;
    default:
      jj_la1[2] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(IDENT);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case WITHIN:
      jj_consume_token(WITHIN);
      deriving = jj_consume_token(IDENT);
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    jj_consume_token(WHERE);
    jj_consume_token(24);
    pred = predicate();
    jj_consume_token(25);
      {if (true) return new Query(name.image, deriving == null ? null : deriving.image, metric, target, pred);}
    throw new Error("Missing return statement in function");
  }

  static final public PredicateMirror predicate() throws ParseException {
    PredicateMirror pred;
    pred = conjunction();
      {if (true) return pred;}
    throw new Error("Missing return statement in function");
  }

  static final public PredicateMirror conjunction() throws ParseException {
    List<PredicateMirror> children = new ArrayList<PredicateMirror>();
    PredicateMirror child;
    child = disjunction();
                              children.add(child);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_2;
      }
      jj_consume_token(AND);
      child = disjunction();
                                     children.add(child);
    }
      {if (true) return new PredicateMirror(PredicateMirror.PredicateType.AND, children);}
    throw new Error("Missing return statement in function");
  }

  static final public PredicateMirror disjunction() throws ParseException {
    List<PredicateMirror> children = new ArrayList<PredicateMirror>();
    PredicateMirror child;
    child = atom_or_conjunction();
                                      children.add(child);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_3;
      }
      jj_consume_token(OR);
      child = atom_or_conjunction();
                                            children.add(child);
    }
      {if (true) return new PredicateMirror(PredicateMirror.PredicateType.OR, children);}
    throw new Error("Missing return statement in function");
  }

  static final public PredicateMirror atom_or_conjunction() throws ParseException {
    PredicateMirror pred;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 26:
      jj_consume_token(26);
      pred = conjunction();
      jj_consume_token(27);
      break;
    case IDENT:
      pred = atom();
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return pred;}
    throw new Error("Missing return statement in function");
  }

  static final public PredicateMirror atom() throws ParseException {
    List<String> attribute = new ArrayList<String>();
    Token component = null;
    Token target = null;
    Token operator = null;
    boolean is = true;
    jj_consume_token(IDENT);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ANY:
      case ALL:
      case THIS:
      case 28:
        ;
        break;
      default:
        jj_la1[7] = jj_gen;
        break label_4;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 28:
        jj_consume_token(28);
        component = jj_consume_token(IDENT);
                                          attribute.add(component.image);
        break;
      case ANY:
        jj_consume_token(ANY);
                                                                                      attribute.add("?");
        break;
      case ALL:
        jj_consume_token(ALL);
                                                                                                                      attribute.add("*");
        break;
      case THIS:
        jj_consume_token(THIS);
                                                                                                                                                       attribute.add("0");
        break;
      default:
        jj_la1[8] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OPERATOR:
      operator = jj_consume_token(OPERATOR);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STRING:
        target = jj_consume_token(STRING);
        break;
      case NUMBER:
        target = jj_consume_token(NUMBER);
        break;
      default:
        jj_la1[9] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    case IS:
      jj_consume_token(IS);
                is =  true;
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        jj_consume_token(NOT);
                                        is = false;
        break;
      default:
        jj_la1[10] = jj_gen;
        ;
      }
      target = jj_consume_token(IDENT);
      break;
    default:
      jj_la1[11] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
        PredicateAtom atom;
        if (operator != null) {
            atom = new PredicateAtom(attribute, operator.image, target.image);
        } else {
            atom = new PredicateAtom(attribute, is, target.image);
        }
        {if (true) return new PredicateMirror(atom);}
    throw new Error("Missing return statement in function");
  }

  static private boolean jj_initialized_once = false;
  /** Generated Token Manager. */
  static public QueryParserTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  /** Current token. */
  static public Token token;
  /** Next token. */
  static public Token jj_nt;
  static private int jj_ntk;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[12];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x80000,0x60,0x180,0x200,0x800,0x1000,0x4080000,0x10070000,0x10070000,0x300000,0x8000,0x402000,};
   }

  /** Constructor with InputStream. */
  public QueryParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public QueryParser(java.io.InputStream stream, String encoding) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new QueryParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public QueryParser(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new QueryParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public QueryParser(QueryParserTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(QueryParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 12; i++) jj_la1[i] = -1;
  }

  static private Token jj_consume_token(int kind) throws ParseException {
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
  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  /** Generate ParseException. */
  static public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[29];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 12; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 29; i++) {
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
  static final public void enable_tracing() {
  }

  /** Disable tracing. */
  static final public void disable_tracing() {
  }

}
