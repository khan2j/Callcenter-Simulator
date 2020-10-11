/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package parser.symbols;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Korrigierte Varianz der Datenreihe, die aus den �bergebenen Parametern gebildet wird
 * @author Alexander Herzog
 */
public final class CalcSymbolPreOperatorVariance extends CalcSymbolPreOperator {
	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length==0) throw error();
		if (parameters.length==1) return 0.0;
		double sum=0, sum2=0;
		for (double d:parameters) {sum+=d; sum2+=d*d;}
		double n=parameters.length;
		return 1/(n-1)*(sum2-sum*sum/n);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) return fallbackValue;
		if (parameters.length==1) return 0.0;
		double sum=0, sum2=0;
		for (double d:parameters) {sum+=d; sum2+=d*d;}
		double n=parameters.length;
		return 1/(n-1)*(sum2-sum*sum/n);
	}

	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"Variance","Varianz","Var"};

	@Override
	public String[] getNames() {
		return names;
	}
}
