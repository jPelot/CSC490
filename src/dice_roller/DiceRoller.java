package dice_roller;

import java.util.Random;

import notation_parser.Die;
import notation_parser.DieExpression;

import notation_parser.Die.Type;

public abstract class DiceRoller {

	static public ResultSet roll(DieExpression expression) {
		Random random = new Random();
		ResultSet result = new ResultSet();
		
		for (Die die : expression.dice()) {
			if (die.type() == Type.constant) {
				result.add(die.getCount(), die);
				continue;
			}
			int sum = 0;
			for(int i = 0; i < die.getCount(); i++) {
				sum += random.nextInt(die.getValue())+1;
			}
			if (die.isNegative()) {sum = sum*-1;}
			result.add(sum, die);
			
		}
		
		return result;
	}
}
