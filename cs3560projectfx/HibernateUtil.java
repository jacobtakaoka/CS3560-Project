package com.example.cs3560projectfx;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    static {
        try {
            // Load configuration and mapped entities
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

            // Register annotated classes
            configuration.addAnnotatedClass(com.example.cs3560projectfx.Student.class);
            configuration.addAnnotatedClass(com.example.cs3560projectfx.Loan.class);
            configuration.addAnnotatedClass(com.example.cs3560projectfx.Book.class);
            configuration.addAnnotatedClass(com.example.cs3560projectfx.BookCopy.class);
            configuration.addAnnotatedClass(com.example.cs3560projectfx.Library.class);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError("Initial SessionFactory creation failed.");
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}

