/*
 * Copyright  2000-2009 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 */
package org.apache.tomcat.util.bcel;

/**
 * Constants for the project, mostly defined in the JVM specification.
 *
 * @version $Id$
 * @author  <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 */
public interface Constants {

  /** Maximum value for an unsigned short.
   */
  public final static int MAX_SHORT = 65535; // 2^16 - 1

  /** Maximum value for an unsigned byte.
   */
  public final static int MAX_BYTE  = 255; // 2^8 - 1

  /** One of the access flags for fields, methods, or classes.
   *  @see #ACC_PUBLIC
   */
  public final static short ACC_FINAL        = 0x0010;

  /** One of the access flags for fields, methods, or classes.
   *  @see #ACC_PUBLIC
   */
  public final static short ACC_INTERFACE    = 0x0200;

  /** One of the access flags for fields, methods, or classes.
   *  @see #ACC_PUBLIC
   */
  public final static short ACC_ABSTRACT     = 0x0400;

  /** One of the access flags for fields, methods, or classes.
   *  @see #ACC_PUBLIC
   */
  public final static short ACC_ENUM         = 0x4000;

  // Applies to classes compiled by new compilers only
  /** One of the access flags for fields, methods, or classes.
   *  @see #ACC_PUBLIC
   */
  public final static short ACC_SUPER        = 0x0020;

  /** One of the access flags for fields, methods, or classes.
   *  @see #ACC_PUBLIC
   */
  public final static short MAX_ACC_FLAG     = ACC_ENUM;

  /** The names of the access flags. */
  public final static String[] ACCESS_NAMES = {
    "public", "private", "protected", "static", "final", "synchronized",
    "volatile", "transient", "native", "interface", "abstract", "strictfp",
    "synthetic", "annotation", "enum"
  };

  /** Marks a constant pool entry as type UTF-8.  */
  public final static byte CONSTANT_Utf8               = 1;

  /** Marks a constant pool entry as type Integer.  */
  public final static byte CONSTANT_Integer            = 3;

  /** Marks a constant pool entry as type Float.  */
  public final static byte CONSTANT_Float              = 4;

  /** Marks a constant pool entry as type Long.  */
  public final static byte CONSTANT_Long               = 5;

  /** Marks a constant pool entry as type Double.  */
  public final static byte CONSTANT_Double             = 6;

  /** Marks a constant pool entry as a Class.  */
  public final static byte CONSTANT_Class              = 7;

  /** Marks a constant pool entry as a Field Reference.  */
  public final static byte CONSTANT_Fieldref           = 9;

  /** Marks a constant pool entry as type String.  */
  public final static byte CONSTANT_String             = 8;

  /** Marks a constant pool entry as a Method Reference.  */
  public final static byte CONSTANT_Methodref          = 10;

  /** Marks a constant pool entry as an Interface Method Reference.  */
  public final static byte CONSTANT_InterfaceMethodref = 11;

  /** Marks a constant pool entry as a name and type.  */
  public final static byte CONSTANT_NameAndType        = 12;

  /** The names of the types of entries in a constant pool. */
  public final static String[] CONSTANT_NAMES = {
    "", "CONSTANT_Utf8", "", "CONSTANT_Integer",
    "CONSTANT_Float", "CONSTANT_Long", "CONSTANT_Double",
    "CONSTANT_Class", "CONSTANT_String", "CONSTANT_Fieldref",
    "CONSTANT_Methodref", "CONSTANT_InterfaceMethodref",
    "CONSTANT_NameAndType" };
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short LDC              = 18;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short LDC_W            = 19;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short LDC2_W           = 20;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ILOAD            = 21;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short LLOAD            = 22;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short FLOAD            = 23;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short DLOAD            = 24;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ALOAD            = 25;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ILOAD_0          = 26;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short LLOAD_0          = 30;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short FLOAD_0          = 34;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short DLOAD_0          = 38;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ALOAD_0          = 42;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ALOAD_3          = 45;

  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ISTORE           = 54;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short LSTORE           = 55;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short FSTORE           = 56;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short DSTORE           = 57;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ASTORE           = 58;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ISTORE_0         = 59;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short LSTORE_0         = 63;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short FSTORE_0         = 67;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short DSTORE_0         = 71;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ASTORE_0         = 75;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ASTORE_3         = 78;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IINC             = 132;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IFEQ             = 153;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IFNE             = 154;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IFLT             = 155;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IFGE             = 156;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IFGT             = 157;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IFLE             = 158;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IF_ICMPEQ        = 159;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IF_ICMPNE        = 160;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IF_ICMPLT        = 161;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IF_ICMPGE        = 162;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IF_ICMPGT        = 163;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IF_ICMPLE        = 164;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IF_ACMPEQ        = 165;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IF_ACMPNE        = 166;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short GOTO             = 167;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short JSR              = 168;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short RET              = 169;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short TABLESWITCH      = 170;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short LOOKUPSWITCH     = 171;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short GETSTATIC        = 178;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short PUTSTATIC        = 179;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short GETFIELD         = 180;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short PUTFIELD         = 181;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short INVOKEVIRTUAL    = 182;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short INVOKESPECIAL    = 183;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short INVOKESTATIC     = 184;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short INVOKEINTERFACE  = 185;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short NEW              = 187;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short NEWARRAY         = 188;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short ANEWARRAY        = 189;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short CHECKCAST        = 192;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short INSTANCEOF       = 193;
  
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short WIDE             = 196;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short MULTIANEWARRAY   = 197;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IFNULL           = 198;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short IFNONNULL        = 199;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short GOTO_W           = 200;
  /** Java VM opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html">Opcode definitions in The Java Virtual Machine Specification</a> */
  public static final short JSR_W            = 201;

  /** JVM internal opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions.doc.html#60105">Reserved opcodes in the Java Virtual Machine Specification</a> */
  public static final short BREAKPOINT                = 202;
  
  /** JVM internal opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions.doc.html#60105">Reserved opcodes in the Java Virtual Machine Specification</a> */
  public static final short IMPDEP1                   = 254;
  /** JVM internal opcode.
   * @see <a href="http://java.sun.com/docs/books/jvms/second_edition/html/Instructions.doc.html#60105">Reserved opcodes in the Java Virtual Machine Specification</a> */
  public static final short IMPDEP2                   = 255;

  /** Illegal opcode. */
  public static final short  UNDEFINED      = -1;
  /** Illegal opcode. */
  public static final short  UNPREDICTABLE  = -2;
  /** Illegal opcode. */
  public static final short  RESERVED       = -3;
  /** Mnemonic for an illegal opcode. */
  public static final String ILLEGAL_OPCODE = "<illegal opcode>";
  /** Mnemonic for an illegal type. */
  public static final String ILLEGAL_TYPE   = "<illegal type>";

  /** Boolean data type. */
  public static final byte T_BOOLEAN = 4;
  /** Char data type. */
  public static final byte T_CHAR    = 5;
  /** Float data type. */
  public static final byte T_FLOAT   = 6;
  /** Double data type. */
  public static final byte T_DOUBLE  = 7;
  /** Byte data type. */
  public static final byte T_BYTE    = 8;
  /** Short data type. */
  public static final byte T_SHORT   = 9;
  /** Int data type. */
  public static final byte T_INT     = 10;
  /** Long data type. */
  public static final byte T_LONG    = 11;

  /** Void data type (non-standard). */
  public static final byte T_VOID      = 12; // Non-standard
  /** Array data type. */
  public static final byte T_ARRAY     = 13;
  /** Object data type. */
  public static final byte T_OBJECT    = 14;
  /** Reference data type (deprecated). */
  public static final byte T_REFERENCE = 14; // Deprecated
  /** Unknown data type. */
  public static final byte T_UNKNOWN   = 15;
  /** Address data type. */
  public static final byte T_ADDRESS   = 16;

  /** The primitive type names corresponding to the T_XX constants,
   * e.g., TYPE_NAMES[T_INT] = "int"
   */
  public static final String[] TYPE_NAMES = {
    ILLEGAL_TYPE, ILLEGAL_TYPE,  ILLEGAL_TYPE, ILLEGAL_TYPE,
    "boolean", "char", "float", "double", "byte", "short", "int", "long",
    "void", "array", "object", "unknown", "address"
  };

  /** The signature characters corresponding to primitive types,
   * e.g., SHORT_TYPE_NAMES[T_INT] = "I"
   */
  public static final String[] SHORT_TYPE_NAMES = {
    ILLEGAL_TYPE, ILLEGAL_TYPE,  ILLEGAL_TYPE, ILLEGAL_TYPE,
    "Z", "C", "F", "D", "B", "S", "I", "J",
    "V", ILLEGAL_TYPE, ILLEGAL_TYPE, ILLEGAL_TYPE
  };

  /**
   * Number of byte code operands for each opcode, i.e., number of bytes after the tag byte
   * itself.  Indexed by opcode, so NO_OF_OPERANDS[BIPUSH] = the number of operands for a bipush
   * instruction.
   */
  public static final short[] NO_OF_OPERANDS = {
    0/*nop*/, 0/*aconst_null*/, 0/*iconst_m1*/, 0/*iconst_0*/,
    0/*iconst_1*/, 0/*iconst_2*/, 0/*iconst_3*/, 0/*iconst_4*/,
    0/*iconst_5*/, 0/*lconst_0*/, 0/*lconst_1*/, 0/*fconst_0*/,
    0/*fconst_1*/, 0/*fconst_2*/, 0/*dconst_0*/, 0/*dconst_1*/,
    1/*bipush*/, 2/*sipush*/, 1/*ldc*/, 2/*ldc_w*/, 2/*ldc2_w*/,
    1/*iload*/, 1/*lload*/, 1/*fload*/, 1/*dload*/, 1/*aload*/,
    0/*iload_0*/, 0/*iload_1*/, 0/*iload_2*/, 0/*iload_3*/,
    0/*lload_0*/, 0/*lload_1*/, 0/*lload_2*/, 0/*lload_3*/,
    0/*fload_0*/, 0/*fload_1*/, 0/*fload_2*/, 0/*fload_3*/,
    0/*dload_0*/, 0/*dload_1*/, 0/*dload_2*/, 0/*dload_3*/,
    0/*aload_0*/, 0/*aload_1*/, 0/*aload_2*/, 0/*aload_3*/,
    0/*iaload*/, 0/*laload*/, 0/*faload*/, 0/*daload*/,
    0/*aaload*/, 0/*baload*/, 0/*caload*/, 0/*saload*/,
    1/*istore*/, 1/*lstore*/, 1/*fstore*/, 1/*dstore*/,
    1/*astore*/, 0/*istore_0*/, 0/*istore_1*/, 0/*istore_2*/,
    0/*istore_3*/, 0/*lstore_0*/, 0/*lstore_1*/, 0/*lstore_2*/,
    0/*lstore_3*/, 0/*fstore_0*/, 0/*fstore_1*/, 0/*fstore_2*/,
    0/*fstore_3*/, 0/*dstore_0*/, 0/*dstore_1*/, 0/*dstore_2*/,
    0/*dstore_3*/, 0/*astore_0*/, 0/*astore_1*/, 0/*astore_2*/,
    0/*astore_3*/, 0/*iastore*/, 0/*lastore*/, 0/*fastore*/,
    0/*dastore*/, 0/*aastore*/, 0/*bastore*/, 0/*castore*/,
    0/*sastore*/, 0/*pop*/, 0/*pop2*/, 0/*dup*/, 0/*dup_x1*/,
    0/*dup_x2*/, 0/*dup2*/, 0/*dup2_x1*/, 0/*dup2_x2*/, 0/*swap*/,
    0/*iadd*/, 0/*ladd*/, 0/*fadd*/, 0/*dadd*/, 0/*isub*/,
    0/*lsub*/, 0/*fsub*/, 0/*dsub*/, 0/*imul*/, 0/*lmul*/,
    0/*fmul*/, 0/*dmul*/, 0/*idiv*/, 0/*ldiv*/, 0/*fdiv*/,
    0/*ddiv*/, 0/*irem*/, 0/*lrem*/, 0/*frem*/, 0/*drem*/,
    0/*ineg*/, 0/*lneg*/, 0/*fneg*/, 0/*dneg*/, 0/*ishl*/,
    0/*lshl*/, 0/*ishr*/, 0/*lshr*/, 0/*iushr*/, 0/*lushr*/,
    0/*iand*/, 0/*land*/, 0/*ior*/, 0/*lor*/, 0/*ixor*/, 0/*lxor*/,
    2/*iinc*/, 0/*i2l*/, 0/*i2f*/, 0/*i2d*/, 0/*l2i*/, 0/*l2f*/,
    0/*l2d*/, 0/*f2i*/, 0/*f2l*/, 0/*f2d*/, 0/*d2i*/, 0/*d2l*/,
    0/*d2f*/, 0/*i2b*/, 0/*i2c*/, 0/*i2s*/, 0/*lcmp*/, 0/*fcmpl*/,
    0/*fcmpg*/, 0/*dcmpl*/, 0/*dcmpg*/, 2/*ifeq*/, 2/*ifne*/,
    2/*iflt*/, 2/*ifge*/, 2/*ifgt*/, 2/*ifle*/, 2/*if_icmpeq*/,
    2/*if_icmpne*/, 2/*if_icmplt*/, 2/*if_icmpge*/, 2/*if_icmpgt*/,
    2/*if_icmple*/, 2/*if_acmpeq*/, 2/*if_acmpne*/, 2/*goto*/,
    2/*jsr*/, 1/*ret*/, UNPREDICTABLE/*tableswitch*/, UNPREDICTABLE/*lookupswitch*/,
    0/*ireturn*/, 0/*lreturn*/, 0/*freturn*/,
    0/*dreturn*/, 0/*areturn*/, 0/*return*/,
    2/*getstatic*/, 2/*putstatic*/, 2/*getfield*/,
    2/*putfield*/, 2/*invokevirtual*/, 2/*invokespecial*/, 2/*invokestatic*/,
    4/*invokeinterface*/, UNDEFINED, 2/*new*/,
    1/*newarray*/, 2/*anewarray*/,
    0/*arraylength*/, 0/*athrow*/, 2/*checkcast*/,
    2/*instanceof*/, 0/*monitorenter*/,
    0/*monitorexit*/, UNPREDICTABLE/*wide*/, 3/*multianewarray*/,
    2/*ifnull*/, 2/*ifnonnull*/, 4/*goto_w*/,
    4/*jsr_w*/, 0/*breakpoint*/, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, RESERVED/*impdep1*/, RESERVED/*impdep2*/
  };

  /**
   * How the byte code operands are to be interpreted for each opcode.
   * Indexed by opcode.  TYPE_OF_OPERANDS[ILOAD] = an array of shorts
   * describing the data types for the instruction.
   */
  public static final short[][] TYPE_OF_OPERANDS = {
    {}/*nop*/, {}/*aconst_null*/, {}/*iconst_m1*/, {}/*iconst_0*/,
    {}/*iconst_1*/, {}/*iconst_2*/, {}/*iconst_3*/, {}/*iconst_4*/,
    {}/*iconst_5*/, {}/*lconst_0*/, {}/*lconst_1*/, {}/*fconst_0*/,
    {}/*fconst_1*/, {}/*fconst_2*/, {}/*dconst_0*/, {}/*dconst_1*/,
    {T_BYTE}/*bipush*/, {T_SHORT}/*sipush*/, {T_BYTE}/*ldc*/,
    {T_SHORT}/*ldc_w*/, {T_SHORT}/*ldc2_w*/,
    {T_BYTE}/*iload*/, {T_BYTE}/*lload*/, {T_BYTE}/*fload*/,
    {T_BYTE}/*dload*/, {T_BYTE}/*aload*/, {}/*iload_0*/,
    {}/*iload_1*/, {}/*iload_2*/, {}/*iload_3*/, {}/*lload_0*/,
    {}/*lload_1*/, {}/*lload_2*/, {}/*lload_3*/, {}/*fload_0*/,
    {}/*fload_1*/, {}/*fload_2*/, {}/*fload_3*/, {}/*dload_0*/,
    {}/*dload_1*/, {}/*dload_2*/, {}/*dload_3*/, {}/*aload_0*/,
    {}/*aload_1*/, {}/*aload_2*/, {}/*aload_3*/, {}/*iaload*/,
    {}/*laload*/, {}/*faload*/, {}/*daload*/, {}/*aaload*/,
    {}/*baload*/, {}/*caload*/, {}/*saload*/, {T_BYTE}/*istore*/,
    {T_BYTE}/*lstore*/, {T_BYTE}/*fstore*/, {T_BYTE}/*dstore*/,
    {T_BYTE}/*astore*/, {}/*istore_0*/, {}/*istore_1*/,
    {}/*istore_2*/, {}/*istore_3*/, {}/*lstore_0*/, {}/*lstore_1*/,
    {}/*lstore_2*/, {}/*lstore_3*/, {}/*fstore_0*/, {}/*fstore_1*/,
    {}/*fstore_2*/, {}/*fstore_3*/, {}/*dstore_0*/, {}/*dstore_1*/,
    {}/*dstore_2*/, {}/*dstore_3*/, {}/*astore_0*/, {}/*astore_1*/,
    {}/*astore_2*/, {}/*astore_3*/, {}/*iastore*/, {}/*lastore*/,
    {}/*fastore*/, {}/*dastore*/, {}/*aastore*/, {}/*bastore*/,
    {}/*castore*/, {}/*sastore*/, {}/*pop*/, {}/*pop2*/, {}/*dup*/,
    {}/*dup_x1*/, {}/*dup_x2*/, {}/*dup2*/, {}/*dup2_x1*/,
    {}/*dup2_x2*/, {}/*swap*/, {}/*iadd*/, {}/*ladd*/, {}/*fadd*/,
    {}/*dadd*/, {}/*isub*/, {}/*lsub*/, {}/*fsub*/, {}/*dsub*/,
    {}/*imul*/, {}/*lmul*/, {}/*fmul*/, {}/*dmul*/, {}/*idiv*/,
    {}/*ldiv*/, {}/*fdiv*/, {}/*ddiv*/, {}/*irem*/, {}/*lrem*/,
    {}/*frem*/, {}/*drem*/, {}/*ineg*/, {}/*lneg*/, {}/*fneg*/,
    {}/*dneg*/, {}/*ishl*/, {}/*lshl*/, {}/*ishr*/, {}/*lshr*/,
    {}/*iushr*/, {}/*lushr*/, {}/*iand*/, {}/*land*/, {}/*ior*/,
    {}/*lor*/, {}/*ixor*/, {}/*lxor*/, {T_BYTE, T_BYTE}/*iinc*/,
    {}/*i2l*/, {}/*i2f*/, {}/*i2d*/, {}/*l2i*/, {}/*l2f*/, {}/*l2d*/,
    {}/*f2i*/, {}/*f2l*/, {}/*f2d*/, {}/*d2i*/, {}/*d2l*/, {}/*d2f*/,
    {}/*i2b*/, {}/*i2c*/,{}/*i2s*/, {}/*lcmp*/, {}/*fcmpl*/,
    {}/*fcmpg*/, {}/*dcmpl*/, {}/*dcmpg*/, {T_SHORT}/*ifeq*/,
    {T_SHORT}/*ifne*/, {T_SHORT}/*iflt*/, {T_SHORT}/*ifge*/,
    {T_SHORT}/*ifgt*/, {T_SHORT}/*ifle*/, {T_SHORT}/*if_icmpeq*/,
    {T_SHORT}/*if_icmpne*/, {T_SHORT}/*if_icmplt*/,
    {T_SHORT}/*if_icmpge*/, {T_SHORT}/*if_icmpgt*/,
    {T_SHORT}/*if_icmple*/, {T_SHORT}/*if_acmpeq*/,
    {T_SHORT}/*if_acmpne*/, {T_SHORT}/*goto*/, {T_SHORT}/*jsr*/,
    {T_BYTE}/*ret*/, {}/*tableswitch*/, {}/*lookupswitch*/,
    {}/*ireturn*/, {}/*lreturn*/, {}/*freturn*/, {}/*dreturn*/,
    {}/*areturn*/, {}/*return*/, {T_SHORT}/*getstatic*/,
    {T_SHORT}/*putstatic*/, {T_SHORT}/*getfield*/,
    {T_SHORT}/*putfield*/, {T_SHORT}/*invokevirtual*/,
    {T_SHORT}/*invokespecial*/, {T_SHORT}/*invokestatic*/,
    {T_SHORT, T_BYTE, T_BYTE}/*invokeinterface*/, {},
    {T_SHORT}/*new*/, {T_BYTE}/*newarray*/,
    {T_SHORT}/*anewarray*/, {}/*arraylength*/, {}/*athrow*/,
    {T_SHORT}/*checkcast*/, {T_SHORT}/*instanceof*/,
    {}/*monitorenter*/, {}/*monitorexit*/, {T_BYTE}/*wide*/,
    {T_SHORT, T_BYTE}/*multianewarray*/, {T_SHORT}/*ifnull*/,
    {T_SHORT}/*ifnonnull*/, {T_INT}/*goto_w*/, {T_INT}/*jsr_w*/,
    {}/*breakpoint*/, {}, {}, {}, {}, {}, {}, {},
    {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
    {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
    {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
    {}/*impdep1*/, {}/*impdep2*/
  };

  /**
   * Names of opcodes.  Indexed by opcode.  OPCODE_NAMES[ALOAD] = "aload".
   */ 
  public static final String[] OPCODE_NAMES = {
    "nop", "aconst_null", "iconst_m1", "iconst_0", "iconst_1",
    "iconst_2", "iconst_3", "iconst_4", "iconst_5", "lconst_0",
    "lconst_1", "fconst_0", "fconst_1", "fconst_2", "dconst_0",
    "dconst_1", "bipush", "sipush", "ldc", "ldc_w", "ldc2_w", "iload",
    "lload", "fload", "dload", "aload", "iload_0", "iload_1", "iload_2",
    "iload_3", "lload_0", "lload_1", "lload_2", "lload_3", "fload_0",
    "fload_1", "fload_2", "fload_3", "dload_0", "dload_1", "dload_2",
    "dload_3", "aload_0", "aload_1", "aload_2", "aload_3", "iaload",
    "laload", "faload", "daload", "aaload", "baload", "caload", "saload",
    "istore", "lstore", "fstore", "dstore", "astore", "istore_0",
    "istore_1", "istore_2", "istore_3", "lstore_0", "lstore_1",
    "lstore_2", "lstore_3", "fstore_0", "fstore_1", "fstore_2",
    "fstore_3", "dstore_0", "dstore_1", "dstore_2", "dstore_3",
    "astore_0", "astore_1", "astore_2", "astore_3", "iastore", "lastore",
    "fastore", "dastore", "aastore", "bastore", "castore", "sastore",
    "pop", "pop2", "dup", "dup_x1", "dup_x2", "dup2", "dup2_x1",
    "dup2_x2", "swap", "iadd", "ladd", "fadd", "dadd", "isub", "lsub",
    "fsub", "dsub", "imul", "lmul", "fmul", "dmul", "idiv", "ldiv",
    "fdiv", "ddiv", "irem", "lrem", "frem", "drem", "ineg", "lneg",
    "fneg", "dneg", "ishl", "lshl", "ishr", "lshr", "iushr", "lushr",
    "iand", "land", "ior", "lor", "ixor", "lxor", "iinc", "i2l", "i2f",
    "i2d", "l2i", "l2f", "l2d", "f2i", "f2l", "f2d", "d2i", "d2l", "d2f",
    "i2b", "i2c", "i2s", "lcmp", "fcmpl", "fcmpg",
    "dcmpl", "dcmpg", "ifeq", "ifne", "iflt", "ifge", "ifgt", "ifle",
    "if_icmpeq", "if_icmpne", "if_icmplt", "if_icmpge", "if_icmpgt",
    "if_icmple", "if_acmpeq", "if_acmpne", "goto", "jsr", "ret",
    "tableswitch", "lookupswitch", "ireturn", "lreturn", "freturn",
    "dreturn", "areturn", "return", "getstatic", "putstatic", "getfield",
    "putfield", "invokevirtual", "invokespecial", "invokestatic",
    "invokeinterface", ILLEGAL_OPCODE, "new", "newarray", "anewarray",
    "arraylength", "athrow", "checkcast", "instanceof", "monitorenter",
    "monitorexit", "wide", "multianewarray", "ifnull", "ifnonnull",
    "goto_w", "jsr_w", "breakpoint", ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
    ILLEGAL_OPCODE, "impdep1", "impdep2"
  };

  /**
   * Number of words consumed on operand stack by instructions.
   * Indexed by opcode.  CONSUME_STACK[FALOAD] = number of words
   * consumed from the stack by a faload instruction.
   */ 
  public static final int[] CONSUME_STACK = {
    0/*nop*/, 0/*aconst_null*/, 0/*iconst_m1*/, 0/*iconst_0*/, 0/*iconst_1*/,
    0/*iconst_2*/, 0/*iconst_3*/, 0/*iconst_4*/, 0/*iconst_5*/, 0/*lconst_0*/,
    0/*lconst_1*/, 0/*fconst_0*/, 0/*fconst_1*/, 0/*fconst_2*/, 0/*dconst_0*/,
    0/*dconst_1*/, 0/*bipush*/, 0/*sipush*/, 0/*ldc*/, 0/*ldc_w*/, 0/*ldc2_w*/, 0/*iload*/,
    0/*lload*/, 0/*fload*/, 0/*dload*/, 0/*aload*/, 0/*iload_0*/, 0/*iload_1*/, 0/*iload_2*/,
    0/*iload_3*/, 0/*lload_0*/, 0/*lload_1*/, 0/*lload_2*/, 0/*lload_3*/, 0/*fload_0*/,
    0/*fload_1*/, 0/*fload_2*/, 0/*fload_3*/, 0/*dload_0*/, 0/*dload_1*/, 0/*dload_2*/,
    0/*dload_3*/, 0/*aload_0*/, 0/*aload_1*/, 0/*aload_2*/, 0/*aload_3*/, 2/*iaload*/,
    2/*laload*/, 2/*faload*/, 2/*daload*/, 2/*aaload*/, 2/*baload*/, 2/*caload*/, 2/*saload*/,
    1/*istore*/, 2/*lstore*/, 1/*fstore*/, 2/*dstore*/, 1/*astore*/, 1/*istore_0*/,
    1/*istore_1*/, 1/*istore_2*/, 1/*istore_3*/, 2/*lstore_0*/, 2/*lstore_1*/,
    2/*lstore_2*/, 2/*lstore_3*/, 1/*fstore_0*/, 1/*fstore_1*/, 1/*fstore_2*/,
    1/*fstore_3*/, 2/*dstore_0*/, 2/*dstore_1*/, 2/*dstore_2*/, 2/*dstore_3*/,
    1/*astore_0*/, 1/*astore_1*/, 1/*astore_2*/, 1/*astore_3*/, 3/*iastore*/, 4/*lastore*/,
    3/*fastore*/, 4/*dastore*/, 3/*aastore*/, 3/*bastore*/, 3/*castore*/, 3/*sastore*/,
    1/*pop*/, 2/*pop2*/, 1/*dup*/, 2/*dup_x1*/, 3/*dup_x2*/, 2/*dup2*/, 3/*dup2_x1*/,
    4/*dup2_x2*/, 2/*swap*/, 2/*iadd*/, 4/*ladd*/, 2/*fadd*/, 4/*dadd*/, 2/*isub*/, 4/*lsub*/,
    2/*fsub*/, 4/*dsub*/, 2/*imul*/, 4/*lmul*/, 2/*fmul*/, 4/*dmul*/, 2/*idiv*/, 4/*ldiv*/,
    2/*fdiv*/, 4/*ddiv*/, 2/*irem*/, 4/*lrem*/, 2/*frem*/, 4/*drem*/, 1/*ineg*/, 2/*lneg*/,
    1/*fneg*/, 2/*dneg*/, 2/*ishl*/, 3/*lshl*/, 2/*ishr*/, 3/*lshr*/, 2/*iushr*/, 3/*lushr*/,
    2/*iand*/, 4/*land*/, 2/*ior*/, 4/*lor*/, 2/*ixor*/, 4/*lxor*/, 0/*iinc*/,
    1/*i2l*/, 1/*i2f*/, 1/*i2d*/, 2/*l2i*/, 2/*l2f*/, 2/*l2d*/, 1/*f2i*/, 1/*f2l*/,
    1/*f2d*/, 2/*d2i*/, 2/*d2l*/, 2/*d2f*/, 1/*i2b*/, 1/*i2c*/, 1/*i2s*/, 
    4/*lcmp*/, 2/*fcmpl*/, 2/*fcmpg*/, 4/*dcmpl*/, 4/*dcmpg*/, 1/*ifeq*/, 1/*ifne*/,
    1/*iflt*/, 1/*ifge*/, 1/*ifgt*/, 1/*ifle*/, 2/*if_icmpeq*/, 2/*if_icmpne*/, 2/*if_icmplt*/,
    2 /*if_icmpge*/, 2/*if_icmpgt*/, 2/*if_icmple*/, 2/*if_acmpeq*/, 2/*if_acmpne*/,
    0/*goto*/, 0/*jsr*/, 0/*ret*/, 1/*tableswitch*/, 1/*lookupswitch*/, 1/*ireturn*/,
    2/*lreturn*/, 1/*freturn*/, 2/*dreturn*/, 1/*areturn*/, 0/*return*/, 0/*getstatic*/,
    UNPREDICTABLE/*putstatic*/, 1/*getfield*/, UNPREDICTABLE/*putfield*/,
    UNPREDICTABLE/*invokevirtual*/, UNPREDICTABLE/*invokespecial*/,
    UNPREDICTABLE/*invokestatic*/,
    UNPREDICTABLE/*invokeinterface*/, UNDEFINED, 0/*new*/, 1/*newarray*/, 1/*anewarray*/,
    1/*arraylength*/, 1/*athrow*/, 1/*checkcast*/, 1/*instanceof*/, 1/*monitorenter*/,
    1/*monitorexit*/, 0/*wide*/, UNPREDICTABLE/*multianewarray*/, 1/*ifnull*/, 1/*ifnonnull*/,
    0/*goto_w*/, 0/*jsr_w*/, 0/*breakpoint*/, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNPREDICTABLE/*impdep1*/, UNPREDICTABLE/*impdep2*/
  };

  /**
   * Number of words produced onto operand stack by instructions.
   * Indexed by opcode.  CONSUME_STACK[DALOAD] = number of words
   * consumed from the stack by a daload instruction.
   */ 
  public static final int[] PRODUCE_STACK = {
    0/*nop*/, 1/*aconst_null*/, 1/*iconst_m1*/, 1/*iconst_0*/, 1/*iconst_1*/,
    1/*iconst_2*/, 1/*iconst_3*/, 1/*iconst_4*/, 1/*iconst_5*/, 2/*lconst_0*/,
    2/*lconst_1*/, 1/*fconst_0*/, 1/*fconst_1*/, 1/*fconst_2*/, 2/*dconst_0*/,
    2/*dconst_1*/, 1/*bipush*/, 1/*sipush*/, 1/*ldc*/, 1/*ldc_w*/, 2/*ldc2_w*/, 1/*iload*/,
    2/*lload*/, 1/*fload*/, 2/*dload*/, 1/*aload*/, 1/*iload_0*/, 1/*iload_1*/, 1/*iload_2*/,
    1/*iload_3*/, 2/*lload_0*/, 2/*lload_1*/, 2/*lload_2*/, 2/*lload_3*/, 1/*fload_0*/,
    1/*fload_1*/, 1/*fload_2*/, 1/*fload_3*/, 2/*dload_0*/, 2/*dload_1*/, 2/*dload_2*/,
    2/*dload_3*/, 1/*aload_0*/, 1/*aload_1*/, 1/*aload_2*/, 1/*aload_3*/, 1/*iaload*/,
    2/*laload*/, 1/*faload*/, 2/*daload*/, 1/*aaload*/, 1/*baload*/, 1/*caload*/, 1/*saload*/,
    0/*istore*/, 0/*lstore*/, 0/*fstore*/, 0/*dstore*/, 0/*astore*/, 0/*istore_0*/,
    0/*istore_1*/, 0/*istore_2*/, 0/*istore_3*/, 0/*lstore_0*/, 0/*lstore_1*/,
    0/*lstore_2*/, 0/*lstore_3*/, 0/*fstore_0*/, 0/*fstore_1*/, 0/*fstore_2*/,
    0/*fstore_3*/, 0/*dstore_0*/, 0/*dstore_1*/, 0/*dstore_2*/, 0/*dstore_3*/,
    0/*astore_0*/, 0/*astore_1*/, 0/*astore_2*/, 0/*astore_3*/, 0/*iastore*/, 0/*lastore*/,
    0/*fastore*/, 0/*dastore*/, 0/*aastore*/, 0/*bastore*/, 0/*castore*/, 0/*sastore*/,
    0/*pop*/, 0/*pop2*/, 2/*dup*/, 3/*dup_x1*/, 4/*dup_x2*/, 4/*dup2*/, 5/*dup2_x1*/,
    6/*dup2_x2*/, 2/*swap*/, 1/*iadd*/, 2/*ladd*/, 1/*fadd*/, 2/*dadd*/, 1/*isub*/, 2/*lsub*/,
    1/*fsub*/, 2/*dsub*/, 1/*imul*/, 2/*lmul*/, 1/*fmul*/, 2/*dmul*/, 1/*idiv*/, 2/*ldiv*/,
    1/*fdiv*/, 2/*ddiv*/, 1/*irem*/, 2/*lrem*/, 1/*frem*/, 2/*drem*/, 1/*ineg*/, 2/*lneg*/,
    1/*fneg*/, 2/*dneg*/, 1/*ishl*/, 2/*lshl*/, 1/*ishr*/, 2/*lshr*/, 1/*iushr*/, 2/*lushr*/,
    1/*iand*/, 2/*land*/, 1/*ior*/, 2/*lor*/, 1/*ixor*/, 2/*lxor*/,
    0/*iinc*/, 2/*i2l*/, 1/*i2f*/, 2/*i2d*/, 1/*l2i*/, 1/*l2f*/, 2/*l2d*/, 1/*f2i*/,
    2/*f2l*/, 2/*f2d*/, 1/*d2i*/, 2/*d2l*/, 1/*d2f*/,
    1/*i2b*/, 1/*i2c*/, 1/*i2s*/, 1/*lcmp*/, 1/*fcmpl*/, 1/*fcmpg*/,
    1/*dcmpl*/, 1/*dcmpg*/, 0/*ifeq*/, 0/*ifne*/, 0/*iflt*/, 0/*ifge*/, 0/*ifgt*/, 0/*ifle*/,
    0/*if_icmpeq*/, 0/*if_icmpne*/, 0/*if_icmplt*/, 0/*if_icmpge*/, 0/*if_icmpgt*/,
    0/*if_icmple*/, 0/*if_acmpeq*/, 0/*if_acmpne*/, 0/*goto*/, 1/*jsr*/, 0/*ret*/,
    0/*tableswitch*/, 0/*lookupswitch*/, 0/*ireturn*/, 0/*lreturn*/, 0/*freturn*/,
    0/*dreturn*/, 0/*areturn*/, 0/*return*/, UNPREDICTABLE/*getstatic*/, 0/*putstatic*/,
    UNPREDICTABLE/*getfield*/, 0/*putfield*/, UNPREDICTABLE/*invokevirtual*/,
    UNPREDICTABLE/*invokespecial*/, UNPREDICTABLE/*invokestatic*/,
    UNPREDICTABLE/*invokeinterface*/, UNDEFINED, 1/*new*/, 1/*newarray*/, 1/*anewarray*/,
    1/*arraylength*/, 1/*athrow*/, 1/*checkcast*/, 1/*instanceof*/, 0/*monitorenter*/,
    0/*monitorexit*/, 0/*wide*/, 1/*multianewarray*/, 0/*ifnull*/, 0/*ifnonnull*/,
    0/*goto_w*/, 1/*jsr_w*/, 0/*breakpoint*/, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED,
    UNDEFINED, UNPREDICTABLE/*impdep1*/, UNPREDICTABLE/*impdep2*/
  };

  /** Attributes and their corresponding names.
   */
  public static final byte ATTR_UNKNOWN                       			= -1;
  public static final byte ATTR_SOURCE_FILE                   			= 0;
  public static final byte ATTR_CONSTANT_VALUE                			= 1;
  public static final byte ATTR_CODE                          			= 2;
  public static final byte ATTR_EXCEPTIONS                    			= 3;
  public static final byte ATTR_LINE_NUMBER_TABLE             			= 4;
  public static final byte ATTR_LOCAL_VARIABLE_TABLE          			= 5;
  public static final byte ATTR_INNER_CLASSES                 			= 6;
  public static final byte ATTR_SYNTHETIC                     			= 7;
  public static final byte ATTR_DEPRECATED                    			= 8;
  public static final byte ATTR_PMG                           			= 9;
  public static final byte ATTR_SIGNATURE                     			= 10;
  public static final byte ATTR_STACK_MAP                     			= 11;
  public static final byte ATTR_RUNTIME_VISIBLE_ANNOTATIONS    			= 12;
  public static final byte ATTR_RUNTIMEIN_VISIBLE_ANNOTATIONS  			= 13;
  public static final byte ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS 	= 14;
  public static final byte ATTR_RUNTIMEIN_VISIBLE_PARAMETER_ANNOTATIONS = 15;
  public static final byte ATTR_ANNOTATION_DEFAULT                      = 16;
  public static final byte ATTR_LOCAL_VARIABLE_TYPE_TABLE               = 17;
  public static final byte ATTR_ENCLOSING_METHOD                      	= 18;
  public static final byte ATTR_STACK_MAP_TABLE                         = 19;

  public static final short KNOWN_ATTRIBUTES = 20;

  // TOFO: FIXXXXX
  public static final String[] ATTRIBUTE_NAMES = {
    "SourceFile", "ConstantValue", "Code", "Exceptions",
    "LineNumberTable", "LocalVariableTable",
    "InnerClasses", "Synthetic", "Deprecated",
    "PMGClass", "Signature", "StackMap", 
    "RuntimeVisibleAnnotations", "RuntimeInvisibleAnnotations",
    "RuntimeVisibleParameterAnnotations", "RuntimeInvisibleParameterAnnotations",
    "AnnotationDefault", "LocalVariableTypeTable", "EnclosingMethod", "StackMapTable"
  };

  /** Constants used in the StackMap attribute.
   */
  public static final byte ITEM_Bogus      = 0;
  public static final byte ITEM_Object     = 7;
  public static final byte ITEM_NewObject  = 8;

  public static final String[] ITEM_NAMES = {
    "Bogus", "Integer", "Float", "Double", "Long",
    "Null", "InitObject", "Object", "NewObject" 
  };
  
  /** Constants used to identify StackMapEntry types.
   * 
   * For those types which can specify a range, the 
   * constant names the lowest value.
   */
  public static final int SAME_FRAME = 0; 
  public static final int SAME_LOCALS_1_STACK_ITEM_FRAME = 64; 
  public static final int SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED = 247; 
  public static final int CHOP_FRAME = 248; 
  public static final int SAME_FRAME_EXTENDED = 251; 
  public static final int APPEND_FRAME = 252; 
  public static final int FULL_FRAME = 255; 
  
  /** Constants that define the maximum value of 
   * those constants which store ranges. */
  
  public static final int SAME_FRAME_MAX = 63;
  public static final int SAME_LOCALS_1_STACK_ITEM_FRAME_MAX = 127;
  public static final int CHOP_FRAME_MAX = 250;
  public static final int APPEND_FRAME_MAX = 254;
}
