package questdesigner;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringPropertyBase;
import javafx.beans.value.ObservableObjectValue;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.function.Function;

public class Utils {
    public static <T, R> ObjectBinding<R> createMapperObjectBinding(ObservableObjectValue<T> base, Function<? super T, ? extends R> mapper) {
        return Bindings.createObjectBinding(() -> mapper.apply(base.getValue()), base);
    }

    public static <T extends R, R> ObjectBinding<R> cast(ObservableObjectValue<T> base) {
        return createMapperObjectBinding(base, Function.identity());
    }

    @SuppressWarnings("unchecked")
    @Contract("_ -> fail")
    public static <T extends Throwable, U> U sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    public enum SelectFixer {
        INSTANCE;

        public ReadOnlyStringProperty textProperty() {
            return text;
        }

        private ReadOnlyStringProperty text = new ReadOnlyStringPropertyBase() {
            @Override
            public Object getBean() {
                return SelectFixer.INSTANCE;
            }

            @Override
            public String getName() {
                return "text";
            }

            @Override
            public String get() {
                return "null";
            }
        };

        public ReadOnlyObjectProperty<Object> valueProperty() {
            return value;
        }

        private ReadOnlyObjectProperty<Object> value = new ReadOnlyObjectPropertyBase<Object>() {
            @Override
            public Object getBean() {
                return SelectFixer.INSTANCE;
            }

            @Override
            public String getName() {
                return "value";
            }

            @Override
            public Object get() {
                return null;
            }
        };

        public String getText() {
            return "null";
        }

        public SelectFixer getValue() {
            return this;
        }
    }
}
