package com.mdb_software.helena.travelmemories3.DataBase;

/**
 * Created by Helena on 4/10/2015.
 * Provjerava inpute za registraciju i login
 */
public class CheckRegisterInputs {
    String errorMessage;

    public String checkInputs(String name, String email, String pass){
        if (name != null && !name.isEmpty() && pass != null && !pass.isEmpty()) {
            if (isValidEmail(email) ) {
                if(pass.length()>=6) {

                        if (checkPass(pass, name, email)) {
                            return "true";
                        }else{
                            return errorMessage;
                        }

                }else{
                    return errorMessage = "Please enter at least 6 characters password!";
                }
            }else{
                return errorMessage = "Please enter a valid email!";
            }
        }else{
            return errorMessage = "Please fill all fields!";
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
    public  boolean checkPass (CharSequence target, CharSequence name, CharSequence email){
        if (target.equals(name)){
            errorMessage = "Password cant be the same as name!";
            return false;

        }else if (target.equals(email)){
            errorMessage = "Password cant be the same as email";
            return false;
        }else {
            return true;
        }
    }
}
