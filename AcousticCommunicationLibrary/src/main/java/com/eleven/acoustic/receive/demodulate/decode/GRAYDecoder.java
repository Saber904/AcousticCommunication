package com.eleven.acoustic.receive.demodulate.decode;

/**
 * Created by Eleven on 2016/4/10.
 */
public class GRAYDecoder implements Decoder{
    @Override
    public int[] decode(int[] grayCodes) {

        int[] binCodes = new int[grayCodes.length];
        int[] decCodes = new int[grayCodes.length/4];
        for (int i = 0; i < decCodes.length; i++) {
            for (int j = 0; j < 4; j++) {
                if (j == 0)
                    binCodes[4*i + j] = grayCodes[4*i + j];
                else
                    binCodes[4*i + j] = grayCodes[4*i + j] ^ binCodes[4*i + j - 1];

            }
        }
        for (int i = 0; i < decCodes.length; i++) {
            int hex = 0;
            for (int j = 0; j < 4; j++){
                hex += binCodes[j + 4*i] << (3 - j);
            }
            decCodes[i] = hex;
        }
        return decCodes;
    }
}
