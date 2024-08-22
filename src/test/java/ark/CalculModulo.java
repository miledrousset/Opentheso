
package ark;

import fr.cnrs.opentheso.bdd.helper.ToolsHelper;
import fr.cnrs.opentheso.utils.NoIdCheckDigit;
import org.junit.jupiter.api.Test;

/**
 *
 * @author miledrousset
 */
public class CalculModulo {

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void getModulo() {
        String prefix = "BLH-";
        ToolsHelper toolsHelper = new ToolsHelper();
        String idArk;
        idArk = toolsHelper.getNewId(8, true, true);

        System.out.println("arkCode = " + idArk);
        
        NoIdCheckDigit noIdCheckDigit = new NoIdCheckDigit();
        String checkCode = noIdCheckDigit.getControlCharacter(idArk);
        
        idArk = prefix + idArk + "-" + checkCode;
        
        System.out.println("arkFinal = " + idArk);
    }

}
