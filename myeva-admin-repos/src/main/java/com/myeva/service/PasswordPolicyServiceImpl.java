package com.myeva.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.myeva.service.contract.PasswordPolicyService;

@Service
public class PasswordPolicyServiceImpl implements PasswordPolicyService {
	
    // Caractères possibles dans le mot de passe
    private static final String MAJUSCULES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULES = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHIFFRES = "0123456789";
    private static final String SPECIAUX = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    
	@Autowired
	PasswordEncoder PASSWD_ENCODER;
   
    private final SecureRandom random = new SecureRandom();

    private char getcaraterRandomly(String caracteres) {
        return caracteres.charAt(random.nextInt(caracteres.length()));
    }
    
	@Override
	public String generate(int size) {
	    if (size < 8) {
	        throw new IllegalArgumentException("Password size must be at least 8 characters");
	    }

	    List<Character> passwordChars;
	    String allChars = MAJUSCULES + MINUSCULES + CHIFFRES + SPECIAUX;
	    
	    do {
	        passwordChars = new ArrayList<>(size); // Capacité initiale définie
	        
	        // Ajout des caractères obligatoires
	        passwordChars.add(getcaraterRandomly(MAJUSCULES));
	        passwordChars.add(getcaraterRandomly(MINUSCULES));
	        passwordChars.add(getcaraterRandomly(CHIFFRES));
	        passwordChars.add(getcaraterRandomly(SPECIAUX));
	        
	        // Remplissage aléatoire équilibré
	        for (int i = 4; i < size; i++) {
	            String selectedCategory = switch (random.nextInt(4)) {
	                case 0 -> MAJUSCULES;
	                case 1 -> MINUSCULES;
	                case 2 -> CHIFFRES;
	                case 3 -> SPECIAUX;
	                default -> allChars; // Ne devrait jamais arriver
	            };
	            passwordChars.add(getcaraterRandomly(selectedCategory));
	        }
	        
	        Collections.shuffle(passwordChars, random);
	    } while(!containsMajMinNumSpecialCaracters(passwordChars));
	    
	    return passwordChars.stream()
	                       .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
	                       .toString();
	}
	
	private boolean containsMajMinNumSpecialCaracters(List<Character> password) {
	    boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
	    
	    for (char c : password) {
	        if (MAJUSCULES.indexOf(c) >= 0) hasUpper = true;
	        else if (MINUSCULES.indexOf(c) >= 0) hasLower = true;
	        else if (CHIFFRES.indexOf(c) >= 0) hasDigit = true;
	        else if (SPECIAUX.indexOf(c) >= 0) hasSpecial = true;
	        
	        if (hasUpper && hasLower && hasDigit && hasSpecial) {
	            return true;
	        }
	    }
	    
	    return false;
	}

	@Override
	public boolean check(String password) {
        // Check length
        if (password.length() < 9) {
            return false;
        }
        
        // Check uppercase letter
        Pattern pattern = Pattern.compile("[A-Z]");
        Matcher matcher = pattern.matcher(password);
        if (!matcher.find()) {
            return false;
        }
        
        // Check lowercase letter
        pattern = Pattern.compile("[a-z]");
        matcher = pattern.matcher(password);
        if (!matcher.find()) {
            return false;
        }
        
        // Check digit
        pattern = Pattern.compile("\\d");
        matcher = pattern.matcher(password);
        if (!matcher.find()) {
            return false;
        }
        
        // Check special character
        pattern = Pattern.compile("[^A-Za-z0-9]");
        matcher = pattern.matcher(password);
        if (!matcher.find()) {
            return false;
        }
        
        return true;
	}

	@Override
	public boolean checkEquals(String newPasswd, String currentPasswd) {
		return (PASSWD_ENCODER.matches(newPasswd,currentPasswd));
	}
}
