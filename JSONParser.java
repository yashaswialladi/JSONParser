
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Yashaswi
 */
public class JSONParser {
    private static StringReader reader;
    
    public static Object parse(String json) throws JSONException
    {
        if(json==null)
            throw new JSONException("Method parameter cannot be null");
        try{
            reader = new StringReader(json);
            int c = reader.read();
            if(c=='{')
                return readObject();
            else
                throw new JSONException("Error in JSON Format: '{' expected");
        }catch(IOException e)
        {
            throw new JSONException("Unexpected exception",e);
        }
        finally{
            reader.close();
        }
    }
    
    private static Object readObject() throws JSONException, IOException
    {
        Map<String,Object> map= new HashMap<>();
        boolean comma=false;
        for(;;)
        {
            skipSpaces();
            int c = reader.read();
            if((char)c == '}')
            {
                if(comma)
                    throw new JSONException("Expected Name:value pair");
                return map;
            }
            else if(c==',')
            {
                comma=true;
                continue;
            }
            else if(c==-1)
                throw new JSONException("Expected ',' or } or Name:value pair ");
            comma=false;
            reader.reset();
            String name = (String)readString();
            if(map.containsKey(name))
                throw new JSONException("Duplicate name found:"+name);
            skipSpaces();
            c = reader.read();
            if((char)c!=':')
                throw new JSONException("Expected ':', found "+((char)c));
            skipSpaces();
            Object value = readValue();
            map.put(name, value);
        }
    }
   
    private static Object readValue()throws JSONException,IOException
    {
        reader.mark(1024);
        int c = reader.read();
        if((char)c =='"')
        {
            reader.reset();
            return readString();
        }
        else if((char)c == '{')
          return readObject();
        else if((char)c == '[')
            return readArray();
        else if(c=='t'||c=='f'||c=='n')
        {
            reader.reset();
            return readTrueFalseNull((char)c);
        }
        else if(isDigit((char)c)|| ((char)c == '-'))
        {
            reader.reset();
            Object obj = readNumber();
            return obj;
        }
        else
            throw new JSONException("Illegal character found:"+((char)c));
    }
    
    private static Object readString() throws JSONException, IOException
    {
        int c = reader.read();
        if((char)c!='"')
            throw new JSONException("Expected \". Found "+((char)c));
        StringBuilder sb = new StringBuilder();
        boolean escapeCharacter=false;
        while((c = reader.read())!=-1)
        {
            if(escapeCharacter)
            {
                escapeCharacter=false;
                switch((char)c)
                {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        sb.append(readHex());
                        break;
                    default:
                        System.out.println((char)c);
                        throw new JSONException("Illegal Escape Sequence");
                }
            }
            else if((char)c == '"')
                return sb.toString();
            else if((char)c == '\\')
                escapeCharacter=true;
            else
                sb.append((char)c);
        }
        throw new JSONException("Unterminated String");
    }
    
    private static Object readNumber()throws JSONException, IOException
    {
        StringBuilder sb = new StringBuilder();
        int r = reader.read();
        if((char)r=='-')
            sb.append('-');
        else
            reader.reset();
        boolean decimal=false, exponent=false, firstDecimal=false, firstExp=false;
        while((r=reader.read())!=-1)
        {
            char c =(char)r;
            if(isDigit(c))
            {
                reader.mark(1024);
                if(decimal)
                    decimal=false;
                sb.append(c);
            }
            else if(c == '.'&&!firstDecimal)
            {
                firstDecimal=true;
                sb.append(c);
                decimal=true;
            }
            else if(c == 'e'|| c == 'E'&&!firstExp)
            {
                firstExp=true;
                exponent=true;
                sb.append(c);
            }
            else if((c == '+'|| c == '-')&&exponent)
            {
                exponent=false;
                sb.append(c);
            }
            else if(isTerminator(c))
            {
                if(decimal)
                    throw new JSONException("Expected fractional part");
                else
                {
                    reader.reset();
                    return sb.toString();
                }
            }
            else
            {
                throw new JSONException("Invalid number format");
            }
        }
        return null;
    }
    private static Object readArray() throws JSONException, IOException
    {
        ArrayList array = new ArrayList();
        boolean comma=false;
        for(;;)
        {
            skipSpaces();
            reader.mark(1024);
            int c = reader.read();
            switch (c) {
                case ']':
                    if(comma)
                        throw new JSONException("Value Expected");
                    return array;
                case ',':
                    comma=true;
                    continue;
                default:
                    if(comma)
                        comma=false;
                    reader.reset();
                    Object val = readValue();
                    array.add(val);
                    break;
            }
        }
    }
    
    private static Object readTrueFalseNull(char type) throws JSONException, IOException
    {
        String val = (type=='t') ? "true" : (type=='f') ? "false" : "null";
        boolean wrongformat=false;
        int c;
        for(int i=0;i<val.length();i++)
        {
            c = reader.read();
            if((char)c !=val.charAt(i))
            {
                wrongformat=true;
                break;
            }                
        }
        reader.mark(1024);
        c = reader.read();
        if(isTerminator((char)c)&&!wrongformat)
        {
            reader.reset();
            switch (type) {
                case 't':
                    return true;
                case 'f':
                    return false;
                default:
                    return null;
            }
        }
        else
            throw new JSONException("Wrong value format");
    }
    private static String readHex() throws JSONException, IOException
    {
        int c;
        boolean wrongformat=false;
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<4;i++)
        {
            c = reader.read();
            if((c>=')'&&c<='9')||(c>='A'&&c<='F'))
                sb.append((char)c);
            else
               wrongformat=true;
        }
        if(wrongformat)
            throw new JSONException("Wrong Hex format");
        else
            return String.valueOf(Character.toChars(Integer.parseInt(sb.toString(), 16)));
    }
    private static void skipSpaces()throws IOException
    {
        int c;
        reader.mark(1024);
        while((char)(c=reader.read())==' '||c=='\n'||c=='\t'||c=='\r')
            reader.mark(1024);
        reader.reset();
    }
    private static boolean isDigit(char c)
    {
        return c>='0'&&c<='9';
    }
    
    private static boolean isTerminator(char c)
    {
        return c==','||c==' '||c=='}'||c==']'||c=='\n'||c=='\t';
    }
}
