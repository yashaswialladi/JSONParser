/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Yashaswi
 */
public class JSONException extends Exception{
    
    public JSONException(String message)
    {
        super(message);
    }
    public JSONException(String message, Throwable cause)
    {
        super(message,cause);
    }
}
