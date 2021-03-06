package com.example.demo.utils;

public class UserUtil {
    public static String  vaildatePassword(String password)
    {
        /*密码强度*/
        String power="";
        if(password==null) return power;
        if(password.length()<6||password.length()>18) return power;
        /*合法的组成部分*/
        //String[]collectPart= {"_","@","&","#","$","."};
        String part="@&#$,.";
        for(int i=0;i<password.length();i++)
        {
            /*如果不为字母和数字*/
            if(!(Character.isLetterOrDigit(password.charAt(i))))
            {
                if(!(part.contains(Character.toString(password.charAt(i)))))
                {
                    System.out.println("密码不合法");
                    return power;
                }
            }

        }
        /*纯数字或纯字母*/
        if(isDigit(password)||isletter(password))
        {
            power="★★☆☆☆☆";
        }
        else if(isDigitAndLetter(password))
        {
            power="★★★★☆☆";
        }
        else if(isContainssymble(password))
        {
            power="★★★★★★";
        }

        /*字符串中包含合法的字母和数字*/

        return power;
    }
    /*判断密码强度*/
    /*判断字符串是不是纯数字*/
    private static boolean isDigit(String value)
    {

        for(int i=0;i<value.length();i++)
        {
            if(!Character.isDigit(value.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    /*判断字符串是不是纯字母*/
    private static boolean isletter(String value)
    {

        for(int i=0;i<value.length();i++)
        {
            if(!Character.isLetter(value.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }
    /*有没有包含特殊符号*/

    private static boolean isContainssymble(String value)
    {

        String part="@&#$,.";
        for (int i = 0; i < value.length();i++)
        {
            if(part.contains(Character.toString(value.charAt(i))))
            {
                return true;
            }

        }
        return false;
    }
    /*是否包含数字和字母*/
    private static boolean isDigitAndLetter(String value)
    {
        for(int i=0;i<value.length();i++)
        {
            if(!(Character.isLetterOrDigit(value.charAt(i))))
            {
                return false;
            }
        }
        return true;
    }
    /*是否包含数字字母和特殊符号*/
}
