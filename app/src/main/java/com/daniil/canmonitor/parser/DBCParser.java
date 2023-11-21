package com.daniil.canmonitor.parser;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.daniil.canmonitor.MainActivity;
import com.daniil.canmonitor.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBCParser {


    private Context context;

    public DBCParser(Context context) {
        this.context = context;
    }

    public HashMap<String,Message> parse(String file) {
        boolean boFlag = false;
        HashMap<String, Message> messages = new HashMap();
        if(file.equals("default")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.test)))) {
                String line;
                Message message = null;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("BO_")) {
                        boFlag = true;
                        message = parseMessageString(line);
                        messages.put(message.getId(), message);
                    } else if (line.startsWith(" SG_") && boFlag) {
                        Signal signal = parseSignal(line, message.getId());
                        message.addSignal(signal);
                    } else {
                        boFlag = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(MainActivity.uri);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                Message message = null;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("BO_")) {
                        boFlag = true;
                        message = parseMessageString(line);
                        messages.put(message.getId(), message);
                    } else if (line.startsWith(" SG_") && boFlag) {
                        Signal signal = parseSignal(line, message.getId());
                        message.addSignal(signal);
                    } else {
                        boFlag = false;
                    }
                }
                if(messages.size() == 0){
                    MainActivity.fileDBC = "default";
                    MainActivity.custom = false;
                }
            } catch (IOException e) {
                MainActivity.fileDBC = "default";
                MainActivity.custom = false;
                e.printStackTrace();
            }
        }
        return messages;
    }

    private Message parseMessageString(String message){
        String[] parts = message.split(" ");
        if (parts.length != 4 && parts.length != 5) {
            throw new IllegalArgumentException("Invalid message string: " + message);
        }
        long dec32 = Long.parseLong(parts[1]);
        long dec29 = dec32 & 0x1FFFFFFFL;
        String messageId = "" + dec29;
        String messageName = parts[2].substring(0, parts[2].indexOf(':'));
        int messageLength = Integer.parseInt(parts[3]);
        String senderId = null;
        if (parts.length == 5) {
            senderId = parts[4];
        }
        if (senderId == null) {
            senderId = "null";
        }
        return new Message(message,messageName,messageId,messageLength,senderId);
    }

    public static String idConversion(String canId){
        String result = "";
        String hexNumber = canId;
        int decimalNumber = Integer.parseInt(hexNumber, 16);
        String binaryNumber = String.format("%32s", Integer.toBinaryString(decimalNumber)).replace(' ', '0');
        int bits = Integer.parseInt(binaryNumber, 2);
        int priority = (bits >>> 26) & 0b111;
        int reserved = (bits >> 25) & 1;
        int dp = (bits >> 24) & 1;
        int pf = (bits >>> 16) & 0xFF;
        int ps = (bits >>> 8) & 0xFF;
        int sa = bits & 0xFF;
        int pgn = 0;
        if(pf >= 240){
            int mask4 =  0x3FFFF00;
            pgn = Integer.parseInt(hexNumber,16) & mask4;
            pgn = pgn >> 8;
        }
        else {
            int mask = 0x03FF0000;
            pgn = (Integer.parseInt(hexNumber,16) & mask) >>> 8 ;
        }
        result = result + "Priority: " + priority + "\n"
                + "Reserved: " + reserved + "\n"
                + "DP: " + dp + "\n"
                + "PF: " + pf + "\n"
                + "PS: " + ps + "\n"
                + "SA: " + sa + "\n"
                + "PGN: " + pgn;
        return result;
    }

    private Signal parseSignal(String signalString, String messageId) {
        String name;
        int startBit;
        int length;
        int byteOrder;
        String valueType;
        double factor;
        double offset;
        double min;
        double max;
        String unit;
        String receivers;
        String[] resultParam = new String[11];
        String[] parts = signalString.trim().split("\\s+");
        int count = 0;
        String[] param = new String[6];
        for (int i = 0; i < parts.length; i++){
            if(parts[i].contains("SG_") || parts[i].contains(":")){
            }
            else{
                param[count] = parts[i];
                count++;
            }
        }
        String[] st1 = param[1].split("@");
        String[] stStartAndLenght = st1[0].split("\\|");
        String[] stbyteOrderAndValueType = st1[1].split("");
        param[2] = param[2].replaceAll("[()]","");
        String[] stFactorAndOffset = param[2].split(",");
        param[3] = param[3].replace("[", "").replace("]", "");
        String[] stMinAndMax = param[3].split("\\|");

        resultParam[0] = param[0];
        resultParam[1] = stStartAndLenght[0];
        resultParam[2] = stStartAndLenght[1];
        resultParam[3] = stbyteOrderAndValueType[0];
        resultParam[4] = stbyteOrderAndValueType[1];
        resultParam[5] = stFactorAndOffset[0];
        resultParam[6] = stFactorAndOffset[1];
        resultParam[7] = stMinAndMax[0];
        resultParam[8] = stMinAndMax[1];
        resultParam[9] = param[4];
        resultParam[10] = param[5];
        name = resultParam[0];
        startBit = Integer.parseInt(resultParam[1]);
        length = Integer.parseInt(resultParam[2]);
        byteOrder = Integer.parseInt(resultParam[4]);
        valueType = resultParam[3];
        factor = Double.parseDouble(resultParam[5]);
        offset = Double.parseDouble(resultParam[6]);
        min = Double.parseDouble(resultParam[7]);
        max = Double.parseDouble(resultParam[8]);
        unit = resultParam[9];
        receivers = resultParam[10];
        return new Signal(signalString,messageId,name,startBit,length,byteOrder,valueType,factor,offset,min,max,unit,receivers);
    }
    public static String[] extractCANData(byte[] data, int startBit, int length) {
        String[] result = null;
        int startIndex = startBit / 8;
        int bitsToSkip = startBit % 8;
        int bytesToExtract = (length + bitsToSkip + 7) / 8;
        if (startIndex + bytesToExtract > data.length) {
            return result;
        }else {
            byte[] extractedData = new byte[bytesToExtract];
            int destByte = 0;
            int srcByte = startIndex;
            int srcBit = bitsToSkip;
            while (destByte < bytesToExtract) {
                int remainingBits = length - destByte * 8;
                int bitsToCopy = Math.min(8 - srcBit, remainingBits);
                int mask = (1 << bitsToCopy) - 1;
                int srcByteMasked = (data[srcByte] & 0xff) >> srcBit;
                extractedData[destByte] = (byte) (srcByteMasked & mask);
                destByte++;
                srcBit += bitsToCopy;
                if (srcBit == 8) {
                    srcBit = 0;
                    srcByte++;
                }
            }
            result = new String[bytesToExtract];
            for (int i = 0; i < bytesToExtract; i++) {
                result[i] = String.format("%02X", extractedData[i]);
            }
        }
        return result;
    }

    public static String[] transformData(String[] array, int byteOrder){
        String[] result = null;
        int count = 0;
        if(byteOrder == 0){
            result = array;
        }
        else if(byteOrder == 1){
            result = new String[array.length];
            for(int i = array.length-1; i >= 0; i--){
                result[count] = array[i];
                count++;
            }
        }
        return result;
    }

    public static String transformCanIdToDBCId(String id){
        String result = "";
        long decimal = Long.parseLong(id, 16);
        long dec32 = decimal;
        long dec29 = dec32 & 0x1FFFFFFFL;
        result = result + dec29;
        return result;
    }

    public static long hexArrayToDecimal(String[] hexArray) {
        String hexString = String.join("", hexArray);
        long decimalNumber = Long.parseLong(hexString, 16);
        return decimalNumber;
    }

    public static String calculationOfValues(String[] data, Signal signal){
        String result = "";
        long value_decimal = hexArrayToDecimal(data);
        if(value_decimal >= signal.getMin() && value_decimal <= signal.getMax()){
        result = result + signal.getName() + ": " + (signal.getOffset() + signal.getFactor()*value_decimal) + " " + signal.getUnit() + "\n";
        }
        else result = result + signal.getName() + ": Nan" + signal.getUnit() + "\n";

        return result;
    }
}





