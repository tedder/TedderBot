package net.inervo.TedderBot.NewPageSearch;

/*
 * Copyright (c) 2011, Ted Timmons, Inervo Networks All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * Inervo Networks nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sparkline {
	protected static final int SPARK_UNICODE_VALUES[] = { 9601, 9602, 9603, 9605, 9606, 9607, 9609 };
	protected static final int SPARK_SCALE_UNITS = SPARK_UNICODE_VALUES.length - 1;

	public String getSparkline( List<Double> nums ) {

		double min = Collections.min( nums );
		double max = Collections.max( nums );
		double range = max - min;

		String text = "";
		for ( double num : nums ) {
			int scaled = scale( num, min, range, SPARK_SCALE_UNITS );
			text += new Character( (char) SPARK_UNICODE_VALUES[scaled] );
		}
		return text;
	}

	private int scale( double num, double min, double range, double scaleFactor ) {
		return (int) Math.round( ( ( num - min ) / range ) * SPARK_SCALE_UNITS );
	}

	public static void main( String[] args ) throws Exception {
		List<Double> numbers = new ArrayList<Double>();

		int nums[] = { 4, 1, 4, 1, 2, 4, 3 };
		for ( int n : nums ) {
			numbers.add( (double) n );
		}

		System.out.println( new Sparkline().getSparkline( numbers ) );
		// System.out.println( new Sparkline().getSparkline( intnumbers ) );

	}
}
