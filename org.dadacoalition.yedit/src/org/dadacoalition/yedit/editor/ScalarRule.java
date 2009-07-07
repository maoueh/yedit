package org.dadacoalition.yedit.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dadacoalition.yedit.Activator;
import org.dadacoalition.yedit.preferences.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Scanner rule for matching scalars that are not inside double or single quotes. Scalars
 * inside double or single quotes are taken care of by other rules. Scalars matched by this
 * rule can contain whitespaces and still be part the same scalar token. The scalars cannot
 * span across lines.
 * @author oysteto
 *
 */
public class ScalarRule implements IRule {

    private IToken token;
    private String firstCharRegex = "\\w + / \\. \\ \\( \\) \\? \\@ \\$ _ < = > \\|";
    private String otherCharRegex = "\\w \\s + - / \\. \\ \\( \\) \\? \\@ \\$ _ < = > \\|";
    
    public ScalarRule( IToken token ){
        
        //when in Symfony compatibility mode % are allowed as part of a scalar
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();        
        if( prefs.getBoolean(PreferenceConstants.SYMFONY_COMPATIBILITY_MODE ) ){
            firstCharRegex += " %";
            otherCharRegex += " %";
        }           
        
        this.token = token;
    }
    
    
    public IToken evaluate(ICharacterScanner scanner) {
     
        int c = scanner.read();
        String firstChar = "" + (char) c;

        //the set of characters that are allowed to start a scalar are different than 
        //characters that are allowed in the rest of the scalar, so do a check on the first
        //character first.
        if( !Pattern.matches( "[" + firstCharRegex + "]", firstChar)){
            scanner.unread();
            return Token.UNDEFINED;
        }
        
        Pattern p = Pattern.compile( "[" + otherCharRegex + "]", Pattern.COMMENTS );
        while( c != ICharacterScanner.EOF  ){                    
            String character = "" + (char) c;     

            
            Matcher m = p.matcher(character);
            
            //do not allow this type of scalars to span multiple lines.
            //I think this will make the result more consistent when it comes
            //to parse results between single line damage/repair and whole document
            //damage/repair            
            if( '\n' == (char) c || '\r' == (char) c || !m.matches()){
                scanner.unread();
                break;
            }
            c = scanner.read();            
        }  

        return token;
    }

}
