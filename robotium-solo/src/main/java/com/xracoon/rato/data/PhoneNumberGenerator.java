package com.xracoon.rato.data;
import java.util.Random;

import com.xracoon.rato.DateUtil;


public class PhoneNumberGenerator {
	
    public static String getTelePhoneNum()throws Exception{
    	String phoneNumeber = null;
        int[] yidong={134,135,136,137,138,139,150,151,152,188};
        int[] liantong={130,131,132,155,156,186};
        int[] dianxin={133,153,189};
        //int select=CommonHelper.getRandomNum(3);
        int select= new Random().nextInt(3);
       
        int rand=(int)(Math.random()*8999999+10000000);
        if(select==1){
        	phoneNumeber =  phoneNumeber(yidong, rand,phoneNumeber);
        }else if(select==2){
        	phoneNumeber = phoneNumeber(liantong, rand,phoneNumeber);
        }else{
        	phoneNumeber = phoneNumeber(dianxin, rand,phoneNumeber);
        }
		return phoneNumeber;
    }
    

    private static String phoneNumeber(int[] yun,int rand ,String phoneNumeber ) throws Exception{
    	 int yu=(int)(Math.random()*yun.length);
         int num=yun[yu];
         if (rand<10000000) {
        	 getTelePhoneNum();
         }
         phoneNumeber = String.valueOf(num)+String.valueOf(rand);
         return phoneNumeber;
    }
 
    public static String getPhoneNumByTime()
    {
    	return "19"+DateUtil.format("HHmmssSSS");
    } 
    
//    public static void main(String[] args) throws Exception{
//    	System.out.println(getTelePhoneNum());
//     
//    }
}

