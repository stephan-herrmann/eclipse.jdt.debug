/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;

public class LessEqualOperator extends BinaryOperator {
	public LessEqualOperator(int leftTypeId, int rightTypeId, int start) {
		super(T_boolean, leftTypeId, rightTypeId, false, start);
	}

	/*
	 * @see BinaryOperator#getBooleanResult(IJavaValue, IJavaValue)
	 */
	protected boolean getBooleanResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		switch (getInternResultType()) {
			case T_double :
				return ((IJavaPrimitiveValue) leftOperand).getDoubleValue() <= ((IJavaPrimitiveValue) rightOperand).getDoubleValue();
			case T_float :
				return ((IJavaPrimitiveValue) leftOperand).getFloatValue() <= ((IJavaPrimitiveValue) rightOperand).getFloatValue();
			case T_long :
				return ((IJavaPrimitiveValue) leftOperand).getLongValue() <= ((IJavaPrimitiveValue) rightOperand).getLongValue();
			case T_int :
				return ((IJavaPrimitiveValue) leftOperand).getIntValue() <= ((IJavaPrimitiveValue) rightOperand).getIntValue();
			default :
				return false;
		}
	}

	/*
	 * @see BinaryOperator#getDoubleResult(IJavaValue, IJavaValue)
	 */
	protected double getDoubleResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getFloatResult(IJavaValue, IJavaValue)
	 */
	protected float getFloatResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getIntResult(IJavaValue, IJavaValue)
	 */
	protected int getIntResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getLongResult(IJavaValue, IJavaValue)
	 */
	protected long getLongResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return 0;
	}

	/*
	 * @see BinaryOperator#getStringResult(IJavaValue, IJavaValue)
	 */
	protected String getStringResult(IJavaValue leftOperand, IJavaValue rightOperand) {
		return null;
	}
	public String toString() {
		return InstructionsEvaluationMessages.getString("LessEqualOperator.operator_1"); //$NON-NLS-1$
	}

}