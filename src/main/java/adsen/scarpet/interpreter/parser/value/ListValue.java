package adsen.scarpet.interpreter.parser.value;

import adsen.scarpet.interpreter.parser.exception.InternalExpressionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class ListValue extends AbstractListValue implements ContainerValueInterface {
    protected List<Value> items;

    public ListValue(Collection<? extends Value> list) {
        items = new ArrayList<>();
        items.addAll(list);
    }

    private ListValue() {
        items = new ArrayList<>();
    }

    public static ListValue wrap(List<Value> list) {
        ListValue created = new ListValue();
        created.items = list;
        return created;
    }

    public static ListValue of(Value... list) {
        return ListValue.wrap(Arrays.asList(list));
    }

    public List<Value> unpack(){
        return items;
    }

    /**
     * Finds a proper list index >=0 and < len that correspont to the rolling index value of idx
     */
    public static int normalizeIndex(long idx, int len) {
        if (idx >= 0 && idx < len) return (int) idx;
        long range = abs(idx) / len;
        idx += (range + 2) * len;
        idx = idx % len;
        return (int) idx;
    }

    @Override
    public String getString() {
        return "[" + items.stream().map(Value::getString).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public String getPrettyString() {
        if (items.size() < 8)
            return "[" + items.stream().map(Value::getPrettyString).collect(Collectors.joining(", ")) + "]";
        return "[" + items.get(0).getPrettyString() + ", " + items.get(1).getPrettyString() + ", ..., " +
                items.get(items.size() - 2).getPrettyString() + ", " + items.get(items.size() - 1).getPrettyString() + "]";
    }

    @Override
    public String getTypeString() {
        return "type";
    }

    @Override
    public boolean getBoolean() {
        return !items.isEmpty();
    }

    @Override
    public Value clone() {
        return new ListValue(items);
    }

    @Override
    public Value add(Value other) {
        ListValue output = new ListValue();
        if (other instanceof ListValue) {
            List<Value> other_list = ((ListValue) other).items;
            if (other_list.size() == items.size()) {
                for (int i = 0, size = items.size(); i < size; i++) {
                    output.items.add(items.get(i).add(other_list.get(i)));
                }
            } else {
                throw new InternalExpressionException("Cannot subtract two lists of uneven sizes");
            }
        } else {
            for (Value v : items) {
                output.items.add(v.add(other));
            }
        }
        return output;
    }

    public void append(Value v) {
        if(v instanceof AbstractListValue)
            extend(((AbstractListValue) v).unpack());
        items.add(v);
    }

    public Value subtract(Value other) {
        ListValue output = new ListValue();
        if (other instanceof ListValue) {
            List<Value> other_list = ((ListValue) other).items;
            if (other_list.size() == items.size()) {
                for (int i = 0, size = items.size(); i < size; i++) {
                    output.items.add(items.get(i).subtract(other_list.get(i)));
                }
            } else {
                throw new InternalExpressionException("Cannot subtract two lists of uneven sizes");
            }
        } else {
            for (Value v : items) {
                output.items.add(v.subtract(other));
            }
        }
        return output;
    }

    public void subtractFrom(Value v) // if I ever do -= then it wouod remove items
    {
        throw new UnsupportedOperationException(); // TODO
    }

    public Value multiply(Value other) {
        ListValue output = new ListValue();
        if (other instanceof ListValue) {
            List<Value> other_list = ((ListValue) other).items;
            if (other_list.size() == items.size()) {
                for (int i = 0, size = items.size(); i < size; i++) {
                    output.items.add(items.get(i).multiply(other_list.get(i)));
                }
            } else {
                throw new InternalExpressionException("Cannot subtract two lists of uneven sizes");
            }
        } else {
            for (Value v : items) {
                output.items.add(v.multiply(other));
            }
        }
        return output;
    }

    public Value divide(Value other) {
        ListValue output = new ListValue();
        if (other instanceof ListValue) {
            List<Value> other_list = ((ListValue) other).items;
            if (other_list.size() == items.size()) {
                for (int i = 0, size = items.size(); i < size; i++) {
                    output.items.add(items.get(i).divide(other_list.get(i)));
                }
            } else {
                throw new InternalExpressionException("Cannot subtract two lists of uneven sizes");
            }
        } else {
            for (Value v : items) {
                output.items.add(v.divide(other));
            }
        }
        return output;
    }

    @Override
    public int compareTo(Value o) {
        if (o instanceof ListValue) {
            ListValue ol = (ListValue) o;
            int this_size = this.getItems().size();
            int o_size = ol.getItems().size();
            if (this_size != o_size) return this_size - o_size;
            if (this_size == 0) return 0;
            for (int i = 0; i < this_size; i++) {
                int res = this.items.get(i).compareTo(ol.items.get(i));
                if (res != 0) return res;
            }
            return 0;
        }
        return getString().compareTo(o.getString());
    }

    @Override
    public boolean equals(final Value o) {
        if (o instanceof ListValue) {
            ListValue ol = (ListValue) o;
            int this_size = this.getItems().size();
            int o_size = ol.getItems().size();
            if (this_size != o_size) return false;
            if (this_size == 0) return true;
            for (int i = 0; i < this_size; i++)
                if (!this.items.get(i).equals(ol.items.get(i))) return false;
            return true;
        }
        return false;
    }

    public List<Value> getItems() {
        return items;
    }

    public Iterator<Value> iterator() {
        return items.iterator();
    }

    public void extend(List<Value> subList) {
        items.addAll(subList);
    }

    public void addAtIndex(int index, List<Value> subList) {
        int numitems = items.size();
        long range = abs(index) / numitems;
        index += (range + 2) * numitems;
        index = index % numitems;
        for (Value v : subList) {
            if (index < numitems) {
                items.set(index, v);
            } else {
                items.add(v);
            }
            index++;
        }
    }

    public int length() {
        return items.size();
    }

    @Override
    public Value in(Value value1) {
        for (int i = 0; i < items.size(); i++) {
            Value v = items.get(i);
            if (v.equals(value1)) {
                return new NumericValue(i);
            }
        }
        return Value.NULL;
    }

    @Override
    public Value slice(long from, long to) {
        List<Value> items = getItems();
        int size = items.size();
        if (to < 0 || to > size) to = size;
        if (from < 0 || from > size) from = size;
        if (from > to)
            return ListValue.of();
        return new ListValue(getItems().subList((int) from, (int) to));
    }

    @Override
    public double readNumber() {
        return items.size();
    }

    @Override
    public boolean put(Value where, Value value) {
        Value ret = items.set(NumericValue.asNumber(where, "'address' to a list index").getInt(), value);
        return ret != null;
    }

    @Override
    public Value get(Value where) {
        return items.get(NumericValue.asNumber(where, "'address' to a list index").getInt());
    }

    @Override
    public boolean has(Value where) {
        long index = NumericValue.asNumber(where, "'address' to a list index").getLong();
        return index >= 0 && index < items.size();
    }

    @Override
    public boolean delete(Value where) {
        if (!(where instanceof NumericValue) || items.isEmpty()) return false;
        long index = ((NumericValue) where).getLong();
        items.remove(normalizeIndex(index, items.size()));
        return true;
    }

    public static class ListConstructorValue extends ListValue {
        public ListConstructorValue(Collection<? extends Value> list) {
            super(list);
        }
    }
}
