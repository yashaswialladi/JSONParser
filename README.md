# JSONParser

The project is a generic JSON parser implementation for a coding challenge.

The requirements were:

* Input: JSON String to be parsed
* Method: Static method parse
* Output: object of type Object

I have created a JSONException class for throwing exceptions related to JSON Parsing errors.

Usage:

* Call parse function as -  JSONParser.parse(*JSON String*)
* Typecast returned object to Map\<String,Object\>
* Retrieve values by using get(*String*) method of Map object
