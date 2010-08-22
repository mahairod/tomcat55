/* Generated By:JJTree&JavaCC: Do not edit this line. ELParserTokenManager.java */
package org.apache.el.parser;

/** Token Manager. */
public class ELParserTokenManager implements ELParserConstants
{

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0xcL) != 0L)
         {
            jjmatchedKind = 1;
            return 5;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 35:
         return jjMoveStringLiteralDfa1_0(0x8L);
      case 36:
         return jjMoveStringLiteralDfa1_0(0x4L);
      default :
         return jjMoveNfa_0(7, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 123:
         if ((active0 & 0x4L) != 0L)
            return jjStopAtPos(1, 2);
         else if ((active0 & 0x8L) != 0L)
            return jjStopAtPos(1, 3);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 8;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 7:
                  if ((0xffffffe7ffffffffL & l) != 0L)
                  {
                     if (kind > 1)
                        kind = 1;
                     jjCheckNAddStates(0, 4);
                  }
                  else if ((0x1800000000L & l) != 0L)
                  {
                     if (kind > 1)
                        kind = 1;
                     jjCheckNAdd(5);
                  }
                  if ((0xffffffe7ffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(0, 1);
                  break;
               case 0:
                  if ((0xffffffe7ffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(0, 1);
                  break;
               case 2:
                  if ((0xffffffe7ffffffffL & l) == 0L)
                     break;
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAddStates(0, 4);
                  break;
               case 3:
                  if ((0xffffffe7ffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(3, 4);
                  break;
               case 4:
                  if ((0x1800000000L & l) != 0L)
                     jjCheckNAdd(5);
                  break;
               case 5:
                  if ((0xffffffe7ffffffffL & l) == 0L)
                     break;
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAddStates(5, 8);
                  break;
               case 6:
                  if ((0x1800000000L & l) == 0L)
                     break;
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAddStates(9, 13);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 7:
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAddStates(0, 4);
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddTwoStates(0, 1);
                  else if (curChar == 92)
                  {
                     if (kind > 1)
                        kind = 1;
                     jjCheckNAddStates(14, 17);
                  }
                  break;
               case 0:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddTwoStates(0, 1);
                  break;
               case 1:
                  if (curChar != 92)
                     break;
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAddStates(14, 17);
                  break;
               case 2:
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAddStates(0, 4);
                  break;
               case 3:
                  jjCheckNAddTwoStates(3, 4);
                  break;
               case 5:
                  if ((0xf7ffffffffffffffL & l) == 0L)
                     break;
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAddStates(5, 8);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 7:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(0, 1);
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 1)
                        kind = 1;
                     jjCheckNAddStates(0, 4);
                  }
                  break;
               case 0:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(0, 1);
                  break;
               case 2:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAddStates(0, 4);
                  break;
               case 3:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(3, 4);
                  break;
               case 5:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAddStates(5, 8);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 8 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_1(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x10000L) != 0L)
            return 1;
         if ((active0 & 0x5075555007000L) != 0L)
         {
            jjmatchedKind = 51;
            return 30;
         }
         return -1;
      case 1:
         if ((active0 & 0x5065000007000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 1;
            return 30;
         }
         if ((active0 & 0x10555000000L) != 0L)
            return 30;
         return -1;
      case 2:
         if ((active0 & 0x5005000000000L) != 0L)
            return 30;
         if ((active0 & 0x60000007000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 2;
            return 30;
         }
         return -1;
      case 3:
         if ((active0 & 0x5000L) != 0L)
            return 30;
         if ((active0 & 0x60000002000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 3;
            return 30;
         }
         return -1;
      case 4:
         if ((active0 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 4;
            return 30;
         }
         if ((active0 & 0x20000002000L) != 0L)
            return 30;
         return -1;
      case 5:
         if ((active0 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 5;
            return 30;
         }
         return -1;
      case 6:
         if ((active0 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 6;
            return 30;
         }
         return -1;
      case 7:
         if ((active0 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 7;
            return 30;
         }
         return -1;
      case 8:
         if ((active0 & 0x40000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 8;
            return 30;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_1(int pos, long active0)
{
   return jjMoveNfa_1(jjStopStringLiteralDfa_1(pos, active0), pos + 1);
}
private int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 33:
         jjmatchedKind = 35;
         return jjMoveStringLiteralDfa1_1(0x200000000L);
      case 37:
         return jjStopAtPos(0, 49);
      case 38:
         return jjMoveStringLiteralDfa1_1(0x2000000000L);
      case 40:
         return jjStopAtPos(0, 17);
      case 41:
         return jjStopAtPos(0, 18);
      case 42:
         return jjStopAtPos(0, 43);
      case 43:
         return jjStopAtPos(0, 44);
      case 44:
         return jjStopAtPos(0, 22);
      case 45:
         return jjStopAtPos(0, 45);
      case 46:
         return jjStartNfaWithStates_1(0, 16, 1);
      case 47:
         return jjStopAtPos(0, 47);
      case 58:
         return jjStopAtPos(0, 21);
      case 60:
         jjmatchedKind = 25;
         return jjMoveStringLiteralDfa1_1(0x20000000L);
      case 61:
         return jjMoveStringLiteralDfa1_1(0x80000000L);
      case 62:
         jjmatchedKind = 23;
         return jjMoveStringLiteralDfa1_1(0x8000000L);
      case 63:
         return jjStopAtPos(0, 46);
      case 91:
         return jjStopAtPos(0, 19);
      case 93:
         return jjStopAtPos(0, 20);
      case 97:
         return jjMoveStringLiteralDfa1_1(0x4000000000L);
      case 100:
         return jjMoveStringLiteralDfa1_1(0x1000000000000L);
      case 101:
         return jjMoveStringLiteralDfa1_1(0x20100000000L);
      case 102:
         return jjMoveStringLiteralDfa1_1(0x2000L);
      case 103:
         return jjMoveStringLiteralDfa1_1(0x11000000L);
      case 105:
         return jjMoveStringLiteralDfa1_1(0x40000000000L);
      case 108:
         return jjMoveStringLiteralDfa1_1(0x44000000L);
      case 109:
         return jjMoveStringLiteralDfa1_1(0x4000000000000L);
      case 110:
         return jjMoveStringLiteralDfa1_1(0x1400004000L);
      case 111:
         return jjMoveStringLiteralDfa1_1(0x10000000000L);
      case 116:
         return jjMoveStringLiteralDfa1_1(0x1000L);
      case 124:
         return jjMoveStringLiteralDfa1_1(0x8000000000L);
      case 125:
         return jjStopAtPos(0, 15);
      default :
         return jjMoveNfa_1(0, 0);
   }
}
private int jjMoveStringLiteralDfa1_1(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 38:
         if ((active0 & 0x2000000000L) != 0L)
            return jjStopAtPos(1, 37);
         break;
      case 61:
         if ((active0 & 0x8000000L) != 0L)
            return jjStopAtPos(1, 27);
         else if ((active0 & 0x20000000L) != 0L)
            return jjStopAtPos(1, 29);
         else if ((active0 & 0x80000000L) != 0L)
            return jjStopAtPos(1, 31);
         else if ((active0 & 0x200000000L) != 0L)
            return jjStopAtPos(1, 33);
         break;
      case 97:
         return jjMoveStringLiteralDfa2_1(active0, 0x2000L);
      case 101:
         if ((active0 & 0x10000000L) != 0L)
            return jjStartNfaWithStates_1(1, 28, 30);
         else if ((active0 & 0x40000000L) != 0L)
            return jjStartNfaWithStates_1(1, 30, 30);
         else if ((active0 & 0x400000000L) != 0L)
            return jjStartNfaWithStates_1(1, 34, 30);
         break;
      case 105:
         return jjMoveStringLiteralDfa2_1(active0, 0x1000000000000L);
      case 109:
         return jjMoveStringLiteralDfa2_1(active0, 0x20000000000L);
      case 110:
         return jjMoveStringLiteralDfa2_1(active0, 0x44000000000L);
      case 111:
         return jjMoveStringLiteralDfa2_1(active0, 0x4001000000000L);
      case 113:
         if ((active0 & 0x100000000L) != 0L)
            return jjStartNfaWithStates_1(1, 32, 30);
         break;
      case 114:
         if ((active0 & 0x10000000000L) != 0L)
            return jjStartNfaWithStates_1(1, 40, 30);
         return jjMoveStringLiteralDfa2_1(active0, 0x1000L);
      case 116:
         if ((active0 & 0x1000000L) != 0L)
            return jjStartNfaWithStates_1(1, 24, 30);
         else if ((active0 & 0x4000000L) != 0L)
            return jjStartNfaWithStates_1(1, 26, 30);
         break;
      case 117:
         return jjMoveStringLiteralDfa2_1(active0, 0x4000L);
      case 124:
         if ((active0 & 0x8000000000L) != 0L)
            return jjStopAtPos(1, 39);
         break;
      default :
         break;
   }
   return jjStartNfa_1(0, active0);
}
private int jjMoveStringLiteralDfa2_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_1(0, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 100:
         if ((active0 & 0x4000000000L) != 0L)
            return jjStartNfaWithStates_1(2, 38, 30);
         else if ((active0 & 0x4000000000000L) != 0L)
            return jjStartNfaWithStates_1(2, 50, 30);
         break;
      case 108:
         return jjMoveStringLiteralDfa3_1(active0, 0x6000L);
      case 112:
         return jjMoveStringLiteralDfa3_1(active0, 0x20000000000L);
      case 115:
         return jjMoveStringLiteralDfa3_1(active0, 0x40000000000L);
      case 116:
         if ((active0 & 0x1000000000L) != 0L)
            return jjStartNfaWithStates_1(2, 36, 30);
         break;
      case 117:
         return jjMoveStringLiteralDfa3_1(active0, 0x1000L);
      case 118:
         if ((active0 & 0x1000000000000L) != 0L)
            return jjStartNfaWithStates_1(2, 48, 30);
         break;
      default :
         break;
   }
   return jjStartNfa_1(1, active0);
}
private int jjMoveStringLiteralDfa3_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_1(1, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x1000L) != 0L)
            return jjStartNfaWithStates_1(3, 12, 30);
         break;
      case 108:
         if ((active0 & 0x4000L) != 0L)
            return jjStartNfaWithStates_1(3, 14, 30);
         break;
      case 115:
         return jjMoveStringLiteralDfa4_1(active0, 0x2000L);
      case 116:
         return jjMoveStringLiteralDfa4_1(active0, 0x60000000000L);
      default :
         break;
   }
   return jjStartNfa_1(2, active0);
}
private int jjMoveStringLiteralDfa4_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_1(2, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa5_1(active0, 0x40000000000L);
      case 101:
         if ((active0 & 0x2000L) != 0L)
            return jjStartNfaWithStates_1(4, 13, 30);
         break;
      case 121:
         if ((active0 & 0x20000000000L) != 0L)
            return jjStartNfaWithStates_1(4, 41, 30);
         break;
      default :
         break;
   }
   return jjStartNfa_1(3, active0);
}
private int jjMoveStringLiteralDfa5_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_1(3, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 110:
         return jjMoveStringLiteralDfa6_1(active0, 0x40000000000L);
      default :
         break;
   }
   return jjStartNfa_1(4, active0);
}
private int jjMoveStringLiteralDfa6_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_1(4, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(5, active0);
      return 6;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa7_1(active0, 0x40000000000L);
      default :
         break;
   }
   return jjStartNfa_1(5, active0);
}
private int jjMoveStringLiteralDfa7_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_1(5, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(6, active0);
      return 7;
   }
   switch(curChar)
   {
      case 101:
         return jjMoveStringLiteralDfa8_1(active0, 0x40000000000L);
      default :
         break;
   }
   return jjStartNfa_1(6, active0);
}
private int jjMoveStringLiteralDfa8_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_1(6, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(7, active0);
      return 8;
   }
   switch(curChar)
   {
      case 111:
         return jjMoveStringLiteralDfa9_1(active0, 0x40000000000L);
      default :
         break;
   }
   return jjStartNfa_1(7, active0);
}
private int jjMoveStringLiteralDfa9_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_1(7, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(8, active0);
      return 9;
   }
   switch(curChar)
   {
      case 102:
         if ((active0 & 0x40000000000L) != 0L)
            return jjStartNfaWithStates_1(9, 42, 30);
         break;
      default :
         break;
   }
   return jjStartNfa_1(8, active0);
}
private int jjStartNfaWithStates_1(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_1(state, pos + 1);
}
static final long[] jjbitVec3 = {
   0x1ff00000fffffffeL, 0xffffffffffffc000L, 0xffffffffL, 0x600000000000000L
};
static final long[] jjbitVec4 = {
   0x0L, 0x0L, 0x0L, 0xff7fffffff7fffffL
};
static final long[] jjbitVec5 = {
   0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec6 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffL, 0x0L
};
static final long[] jjbitVec7 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0x0L, 0x0L
};
static final long[] jjbitVec8 = {
   0x3fffffffffffL, 0x0L, 0x0L, 0x0L
};
private int jjMoveNfa_1(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 30;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 8)
                        kind = 8;
                     jjCheckNAddStates(18, 22);
                  }
                  else if ((0x1800000000L & l) != 0L)
                  {
                     if (kind > 51)
                        kind = 51;
                     jjCheckNAddTwoStates(28, 29);
                  }
                  else if (curChar == 39)
                     jjCheckNAddStates(23, 25);
                  else if (curChar == 34)
                     jjCheckNAddStates(26, 28);
                  else if (curChar == 46)
                     jjCheckNAdd(1);
                  break;
               case 30:
                  if ((0x3ff001000000000L & l) != 0L)
                  {
                     if (kind > 52)
                        kind = 52;
                     jjCheckNAdd(29);
                  }
                  if ((0x3ff001000000000L & l) != 0L)
                  {
                     if (kind > 51)
                        kind = 51;
                     jjCheckNAdd(28);
                  }
                  break;
               case 1:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(1, 2);
                  break;
               case 3:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(4);
                  break;
               case 4:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(4);
                  break;
               case 5:
                  if (curChar == 34)
                     jjCheckNAddStates(26, 28);
                  break;
               case 6:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddStates(26, 28);
                  break;
               case 8:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(26, 28);
                  break;
               case 9:
                  if (curChar == 34 && kind > 11)
                     kind = 11;
                  break;
               case 10:
                  if (curChar == 39)
                     jjCheckNAddStates(23, 25);
                  break;
               case 11:
                  if ((0xffffff7fffffffffL & l) != 0L)
                     jjCheckNAddStates(23, 25);
                  break;
               case 13:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(23, 25);
                  break;
               case 14:
                  if (curChar == 39 && kind > 11)
                     kind = 11;
                  break;
               case 15:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 8)
                     kind = 8;
                  jjCheckNAddStates(18, 22);
                  break;
               case 16:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 8)
                     kind = 8;
                  jjCheckNAdd(16);
                  break;
               case 17:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(17, 18);
                  break;
               case 18:
                  if (curChar != 46)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(19, 20);
                  break;
               case 19:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(19, 20);
                  break;
               case 21:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(22);
                  break;
               case 22:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(22);
                  break;
               case 23:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(23, 24);
                  break;
               case 25:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(26);
                  break;
               case 26:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(26);
                  break;
               case 27:
                  if ((0x1800000000L & l) == 0L)
                     break;
                  if (kind > 51)
                     kind = 51;
                  jjCheckNAddTwoStates(28, 29);
                  break;
               case 28:
                  if ((0x3ff001000000000L & l) == 0L)
                     break;
                  if (kind > 51)
                     kind = 51;
                  jjCheckNAdd(28);
                  break;
               case 29:
                  if ((0x3ff001000000000L & l) == 0L)
                     break;
                  if (kind > 52)
                     kind = 52;
                  jjCheckNAdd(29);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 51)
                     kind = 51;
                  jjCheckNAddTwoStates(28, 29);
                  break;
               case 30:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 52)
                        kind = 52;
                     jjCheckNAdd(29);
                  }
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 51)
                        kind = 51;
                     jjCheckNAdd(28);
                  }
                  break;
               case 2:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(29, 30);
                  break;
               case 6:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(26, 28);
                  break;
               case 7:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 8:
                  if (curChar == 92)
                     jjCheckNAddStates(26, 28);
                  break;
               case 11:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(23, 25);
                  break;
               case 12:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 13:
                  if (curChar == 92)
                     jjCheckNAddStates(23, 25);
                  break;
               case 20:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(31, 32);
                  break;
               case 24:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(33, 34);
                  break;
               case 28:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 51)
                     kind = 51;
                  jjCheckNAdd(28);
                  break;
               case 29:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 52)
                     kind = 52;
                  jjCheckNAdd(29);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 51)
                     kind = 51;
                  jjCheckNAddTwoStates(28, 29);
                  break;
               case 30:
                  if (jjCanMove_1(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 51)
                        kind = 51;
                     jjCheckNAdd(28);
                  }
                  if (jjCanMove_1(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 52)
                        kind = 52;
                     jjCheckNAdd(29);
                  }
                  break;
               case 6:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(26, 28);
                  break;
               case 11:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(23, 25);
                  break;
               case 28:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 51)
                     kind = 51;
                  jjCheckNAdd(28);
                  break;
               case 29:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 52)
                     kind = 52;
                  jjCheckNAdd(29);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 30 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   0, 1, 3, 4, 2, 0, 1, 4, 2, 0, 1, 4, 5, 2, 0, 1, 
   2, 6, 16, 17, 18, 23, 24, 11, 12, 14, 6, 7, 9, 3, 4, 21, 
   22, 25, 26, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default :
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec4[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec5[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec6[i2] & l2) != 0L);
      case 51:
         return ((jjbitVec7[i2] & l2) != 0L);
      case 61:
         return ((jjbitVec8[i2] & l2) != 0L);
      default :
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, "\44\173", "\43\173", null, null, null, null, null, null, null, null, 
"\164\162\165\145", "\146\141\154\163\145", "\156\165\154\154", "\175", "\56", "\50", "\51", 
"\133", "\135", "\72", "\54", "\76", "\147\164", "\74", "\154\164", "\76\75", 
"\147\145", "\74\75", "\154\145", "\75\75", "\145\161", "\41\75", "\156\145", "\41", 
"\156\157\164", "\46\46", "\141\156\144", "\174\174", "\157\162", "\145\155\160\164\171", 
"\151\156\163\164\141\156\143\145\157\146", "\52", "\53", "\55", "\77", "\57", "\144\151\166", "\45", "\155\157\144", null, 
null, null, null, null, null, };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
   "IN_EXPRESSION",
};

/** Lex State array. */
public static final int[] jjnewLexState = {
   -1, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, 
};
static final long[] jjtoToken = {
   0x11ffffffffffb0fL, 
};
static final long[] jjtoSkip = {
   0xf0L, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[30];
private final int[] jjstateSet = new int[60];
protected char curChar;
/** Constructor. */
public ELParserTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public ELParserTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 30; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 2 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   switch(curLexState)
   {
     case 0:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_0();
       break;
     case 1:
       try { input_stream.backup(0);
          while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
             curChar = input_stream.BeginToken();
       }
       catch (java.io.IOException e1) { continue EOFLoop; }
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_1();
       if (jjmatchedPos == 0 && jjmatchedKind > 56)
       {
          jjmatchedKind = 56;
       }
       break;
   }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else
        {
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}
