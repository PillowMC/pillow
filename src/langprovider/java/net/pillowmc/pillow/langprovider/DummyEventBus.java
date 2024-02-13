/*
 * MIT License
 *
 * Copyright (c) 2024 PillowMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pillowmc.pillow.langprovider;

import java.util.function.Consumer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.IGenericEvent;

@SuppressWarnings("deprecation")
public class DummyEventBus implements IEventBus {
	@Override
	public void register(Object target) {
	}

	@Override
	public <T extends Event> void addListener(Consumer<T> consumer) {
	}

	@Override
	public <T extends Event> void addListener(Class<T> eventType, Consumer<T> consumer) {
	}

	@Override
	public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
	}

	@Override
	public <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> consumer) {
	}

	@Override
	public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Consumer<T> consumer) {
	}

	@Override
	public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType,
			Consumer<T> consumer) {
	}

	@Override
	public <T extends Event> void addListener(boolean receiveCanceled, Consumer<T> consumer) {
	}

	@Override
	public <T extends Event> void addListener(boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer) {
	}

	@Override
	public <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter,
			Consumer<T> consumer) {
	}

	@Override
	public <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter,
			EventPriority priority, Consumer<T> consumer) {
	}

	@Override
	public <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter,
			EventPriority priority, boolean receiveCanceled, Consumer<T> consumer) {
	}

	@Override
	public <T extends Event & IGenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter,
			EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer) {
	}

	@Override
	public void unregister(Object object) {
	}

	@Override
	public <T extends Event> T post(T event) {
		return null;
	}

	@Override
	public <T extends Event> T post(EventPriority phase, T event) {
		return null;
	}

	@Override
	public void start() {
	}
}
