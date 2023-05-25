
package fr.cnrs.opentheso.utils;

/**
 *
 * @author miledrousset
 */
public class NoIdCheckDigit {
    
    public static final int NOID_NUMBER_0 = 0;
    public static final int NOID_NUMBER_1 = 1;
    public static final int NOID_NUMBER_2 = 2;
    public static final int NOID_NUMBER_3 = 3;
    public static final int NOID_NUMBER_4 = 4;
    public static final int NOID_NUMBER_5 = 5;
    public static final int NOID_NUMBER_6 = 6;
    public static final int NOID_NUMBER_7 = 7;
    public static final int NOID_NUMBER_8 = 8;
    public static final int NOID_NUMBER_9 = 9;
    
    public static final int NOID_NUMBER_B = 10;
    public static final int NOID_NUMBER_C = 11;
    public static final int NOID_NUMBER_D = 12;
    public static final int NOID_NUMBER_F = 13;
    public static final int NOID_NUMBER_G = 14;
    public static final int NOID_NUMBER_H = 15;
    public static final int NOID_NUMBER_J = 16;
    public static final int NOID_NUMBER_K = 17;
    public static final int NOID_NUMBER_L = 18;    
    public static final int NOID_NUMBER_M = 19;
    public static final int NOID_NUMBER_N = 20;
    public static final int NOID_NUMBER_P = 21;
    public static final int NOID_NUMBER_Q = 22;
    public static final int NOID_NUMBER_R = 23;
    public static final int NOID_NUMBER_S = 24;
    public static final int NOID_NUMBER_T = 25;
    public static final int NOID_NUMBER_V = 26;
    public static final int NOID_NUMBER_W = 27;
    public static final int NOID_NUMBER_X = 28;
    public static final int NOID_NUMBER_Z = 29;  
    // total général (10 + 20 = 30)    
    
    public static final char NOID_CHAR_0 = '0';
    public static final char NOID_CHAR_1 = '1';
    public static final char NOID_CHAR_2 = '2';
    public static final char NOID_CHAR_3 = '3';
    public static final char NOID_CHAR_4 = '4';
    public static final char NOID_CHAR_5 = '5';
    public static final char NOID_CHAR_6 = '6';
    public static final char NOID_CHAR_7 = '7';
    public static final char NOID_CHAR_8 = '8';
    public static final char NOID_CHAR_9 = '9';
    
    public static final char NOID_CHAR_B = 'b';
    public static final char NOID_CHAR_C = 'c';
    public static final char NOID_CHAR_D = 'd';
    public static final char NOID_CHAR_F = 'f';
    public static final char NOID_CHAR_G = 'g';
    public static final char NOID_CHAR_H = 'h';
    public static final char NOID_CHAR_J = 'j';
    public static final char NOID_CHAR_K = 'k';
    public static final char NOID_CHAR_L = 'l';    
    public static final char NOID_CHAR_M = 'm';
    public static final char NOID_CHAR_N = 'n';
    public static final char NOID_CHAR_P = 'p';
    public static final char NOID_CHAR_Q = 'q';
    public static final char NOID_CHAR_R = 'r';
    public static final char NOID_CHAR_S = 's';
    public static final char NOID_CHAR_T = 't';
    public static final char NOID_CHAR_V = 'v';
    public static final char NOID_CHAR_W = 'w';
    public static final char NOID_CHAR_X = 'x';
    public static final char NOID_CHAR_Z = 'z';      
    

    public NoIdCheckDigit() {
    }

    public String getControlCharacter(String idArk) {
        char[] arrayIdArk = idArk.toLowerCase().toCharArray();
        int tot = 0;
        
        for (int pos = 0; pos < arrayIdArk.length; pos++) {
            switch ((int) arrayIdArk[pos]) {
                case NOID_CHAR_0:
                    tot = tot + ((pos+1) * NOID_NUMBER_0);
                    break;
                case NOID_CHAR_1:
                    tot = tot + ((pos+1) * NOID_NUMBER_1);
                    break;
                case NOID_CHAR_2:
                    tot = tot + ((pos+1) * NOID_NUMBER_2);
                    break;  
                case NOID_CHAR_3:
                    tot = tot + ((pos+1) * NOID_NUMBER_3);
                    break; 
                case NOID_CHAR_4:
                    tot = tot + ((pos+1) * NOID_NUMBER_4);
                    break; 
                case NOID_CHAR_5:
                    tot = tot + ((pos+1) * NOID_NUMBER_5);
                    break; 
                case NOID_CHAR_6:
                    tot = tot + ((pos+1) * NOID_NUMBER_6);
                    break; 
                case NOID_CHAR_7:
                    tot = tot + ((pos+1) * NOID_NUMBER_7);
                    break; 
                case NOID_CHAR_8:
                    tot = tot + ((pos+1) * NOID_NUMBER_8);
                    break; 
                case NOID_CHAR_9:
                    tot = tot + ((pos+1) * NOID_NUMBER_9);
                    break;                     
                    
                case NOID_CHAR_B:
                    tot = tot + ((pos+1) * NOID_NUMBER_B);
                    break;
                case NOID_CHAR_C:
                    tot = tot + ((pos+1) * NOID_NUMBER_C);
                    break;                
                case NOID_CHAR_D:
                    tot = tot + ((pos+1) * NOID_NUMBER_D);
                    break;                
                case NOID_CHAR_F:
                    tot = tot + ((pos+1) * NOID_NUMBER_F);
                    break;                     
                case NOID_CHAR_G:
                    tot = tot + ((pos+1) * NOID_NUMBER_G);
                    break;                  
                case NOID_CHAR_H:
                    tot = tot + ((pos+1) * NOID_NUMBER_H);
                    break;                     
                case NOID_CHAR_J:
                    tot = tot + ((pos+1) * NOID_NUMBER_J);
                    break;                     
                case NOID_CHAR_K:
                    tot = tot + ((pos+1) * NOID_NUMBER_K);
                    break;                     
                case NOID_CHAR_M:
                    tot = tot + ((pos+1) * NOID_NUMBER_M);
                    break;                     
                case NOID_CHAR_N:
                    tot = tot + ((pos+1) * NOID_NUMBER_N);
                    break;                     
                case NOID_CHAR_P:
                    tot = tot + ((pos+1)* NOID_NUMBER_P);
                    break;                     
                case NOID_CHAR_Q:
                    tot = tot + ((pos+1) * NOID_NUMBER_Q);
                    break;                     
                case NOID_CHAR_R:
                    tot = tot + ((pos+1) * NOID_NUMBER_R);
                    break;                     
                case NOID_CHAR_S:
                    tot = tot + ((pos+1) * NOID_NUMBER_S);
                    break;                     
                case NOID_CHAR_T:
                    tot = tot + ((pos+1) * NOID_NUMBER_T);
                    break;                
                case NOID_CHAR_V:
                    tot = tot + ((pos+1) * NOID_NUMBER_V);
                    break;                     
                case NOID_CHAR_W:
                    tot = tot + ((pos+1) * NOID_NUMBER_W);
                    break;                     
                case NOID_CHAR_X:
                    tot = tot + ((pos+1) * NOID_NUMBER_X);
                    break;                     
                case NOID_CHAR_Z:
                    tot = tot + ((pos+1) * NOID_NUMBER_Z);
                    break;                     
                default:
                    tot = tot + ((pos+1) * NOID_NUMBER_0);
                    break;
            }            
        }
        int remainder = tot % 30;
  //      System.out.println("total = " + tot);
  //      System.out.println("remainder = " + remainder);
        
        return getStringControlCharacter(remainder);
    }
    
    private String getStringControlCharacter(int remainder){
        String checkCode;
        switch (remainder) {
            case NOID_NUMBER_0:
                checkCode = "" + NOID_CHAR_0;
                break;
            case NOID_NUMBER_1:
                checkCode = "" + NOID_CHAR_1;
                break;
            case NOID_NUMBER_2:
                checkCode = "" + NOID_CHAR_2;
                break;  
            case NOID_NUMBER_3:
                checkCode = "" + NOID_CHAR_3;
                break; 
            case NOID_NUMBER_4:
                checkCode = "" + NOID_CHAR_4;
                break; 
            case NOID_NUMBER_5:
                checkCode = "" + NOID_CHAR_5;
                break; 
            case NOID_NUMBER_6:
                checkCode = "" + NOID_CHAR_6;
                break; 
            case NOID_NUMBER_7:
                checkCode = "" + NOID_CHAR_7;
                break; 
            case NOID_NUMBER_8:
                checkCode = "" + NOID_CHAR_8;
                break; 
            case NOID_NUMBER_9:
                checkCode = "" + NOID_CHAR_9;
                break;                     

            case NOID_NUMBER_B:
                checkCode = "" + NOID_CHAR_B;
                break;
            case NOID_NUMBER_C:
                checkCode = "" + NOID_CHAR_C;
                break;                
            case NOID_NUMBER_D:
                checkCode = "" + NOID_CHAR_D;
                break;                
            case NOID_NUMBER_F:
                checkCode = "" + NOID_CHAR_F;
                break;                     
            case NOID_NUMBER_G:
                checkCode = "" + NOID_CHAR_G;
                break;                  
            case NOID_NUMBER_H:
                checkCode = "" + NOID_CHAR_H;
                break;                     
            case NOID_NUMBER_J:
                checkCode = "" + NOID_CHAR_J;
                break;                     
            case NOID_NUMBER_K:
                checkCode = "" + NOID_CHAR_K;
                break;                     
            case NOID_NUMBER_M:
                checkCode = "" + NOID_CHAR_M;
                break;                     
            case NOID_NUMBER_N:
                checkCode = "" + NOID_CHAR_N;
                break;                     
            case NOID_NUMBER_P:
                checkCode = "" + NOID_CHAR_P;
                break;                     
            case NOID_NUMBER_Q:
                checkCode = "" + NOID_CHAR_Q;
                break;                     
            case NOID_NUMBER_R:
                checkCode = "" + NOID_CHAR_R;
                break;                     
            case NOID_NUMBER_S:
                checkCode = "" + NOID_CHAR_S;
                break;                     
            case NOID_NUMBER_T:
                checkCode = "" + NOID_CHAR_T;
                break;                
            case NOID_NUMBER_V:
                checkCode = "" + NOID_CHAR_V;
                break;                     
            case NOID_NUMBER_W:
                checkCode = "" + NOID_CHAR_W;
                break;                     
            case NOID_NUMBER_X:
                checkCode = "" + NOID_CHAR_X;
                break;                     
            case NOID_NUMBER_Z:
                checkCode = "" + NOID_CHAR_Z;
                break;                     
            default:
                checkCode = "" + NOID_CHAR_0;
                break;
        }           
        return checkCode;
    }
    
}
