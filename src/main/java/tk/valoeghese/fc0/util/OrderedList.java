/**
 * [From Toaru Kagaku no Mod, by Me]
 * Copyright 2020 Valoeghese
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * - The above copyright notice and this permission notice shall be
 *   included in all copies or substantial portions of the Software.
 * 
 * - THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *   INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 *   PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *   CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package tk.valoeghese.fc0.util;

import java.util.ArrayList;

public class OrderedList<E> extends ArrayList<E> {
	private static final long serialVersionUID = -1899577674990521618L;

	public OrderedList(ToFloatFunction<E> orderFunction) {
		super();
		this.order = orderFunction;
	}

	private final ToFloatFunction<E> order;

	private boolean addOrdered(E e) {
		int size = this.size();

		// special case: no items in the list
		if (size == 0) {
			return super.add(e);
		}

		float value = this.order.apply(e);
		int i = 0;
		float posValue;

		// get the position, i, a multiple of SEARCH_GAP or maximum size, for the first high value above the value of the input
		do {
			posValue = this.order.apply(this.get(i));
			i += SEARCH_GAP;
		} while (posValue < value && i < size);

		// make sure we won't get index out of bounds
		if (i >= size) {
			i = size - 1;
		}

		// count down until we have found a position to insert
		for (;i >= 0 && this.order.apply(this.get(i)) > value;--i) {
		}
		i++; // because

		//System.out.println(i + "\t" + value);
		super.add(i, e);
		return true;
	}

	@Override
	public boolean add(E e) {
		return this.addOrdered(e);
	}

	@Override
	@Deprecated
	public void add(int index, E element) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Don't use add(int, Object) in an ordered list!");
	}

	private static final int SEARCH_GAP = 32;

	@FunctionalInterface	
	public static interface ToFloatFunction<E> {
		float apply(E value);
	}
}
