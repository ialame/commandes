package com.pcagrade.order.ulid;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * Type Hibernate personnalisé pour ULID stocké en BINARY(16)
 */
public class UlidType implements UserType<Ulid> {

    @Override
    public int getSqlType() {
        return Types.BINARY;  // ✅ Changé de VARCHAR à BINARY
    }

    @Override
    public Class<Ulid> returnedClass() {
        return Ulid.class;
    }

    @Override
    public boolean equals(Ulid x, Ulid y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Ulid x) throws HibernateException {
        return Objects.hashCode(x);
    }

    @Override
    public Ulid nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        byte[] bytes = rs.getBytes(position);  // ✅ Changé de getString à getBytes
        return bytes != null ? Ulid.fromBytes(bytes) : null;  // ✅ Utilise fromBytes
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Ulid value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.BINARY);  // ✅ Changé de VARCHAR à BINARY
        } else {
            st.setBytes(index, value.getBytes());  // ✅ Changé de setString à setBytes
        }
    }

    @Override
    public Ulid deepCopy(Ulid value) throws HibernateException {
        return value; // ULID est immutable
    }

    @Override
    public boolean isMutable() {
        return false; // ULID est immutable
    }

    @Override
    public Serializable disassemble(Ulid value) throws HibernateException {
        return value; // ULID est déjà Serializable
    }

    @Override
    public Ulid assemble(Serializable cached, Object owner) throws HibernateException {
        return (Ulid) cached;
    }
}