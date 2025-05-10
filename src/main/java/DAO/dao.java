package DAO;

import java.util.List;

public interface dao<T> {
    void create(T t);
    T read();
    List<T> read_all();
    void update(T t);
    void delete(int id);
}
