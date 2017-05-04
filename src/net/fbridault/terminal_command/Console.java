package net.fbridault.terminal_command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by felix on 04/05/2017.
 */
public abstract class Console {

    private static final String INT_PATTERN = "[0-9]+";
    private static final String FLOAT_PATTERN = "[0-9]*(\\.[0-9]*)?";
    private static final String CHAR_PATTERN = ".";
    private static final String BOOL_PATTERN = "true|false";
    private static final String STRING_PATTERN = ".+";

    private static final Map<Type, String> TYPE_PATTERN_MAP = new HashMap<>();
    private static final Map<Type, Parser> PARSER_MAP = new HashMap<>();
    static {
        TYPE_PATTERN_MAP.put(int.class, INT_PATTERN);
        TYPE_PATTERN_MAP.put(byte.class, INT_PATTERN);
        TYPE_PATTERN_MAP.put(long.class, INT_PATTERN);
        TYPE_PATTERN_MAP.put(short.class, INT_PATTERN);
        TYPE_PATTERN_MAP.put(String.class, STRING_PATTERN);
        TYPE_PATTERN_MAP.put(boolean.class, BOOL_PATTERN);
        TYPE_PATTERN_MAP.put(float.class, FLOAT_PATTERN);
        TYPE_PATTERN_MAP.put(double.class, FLOAT_PATTERN);

        PARSER_MAP.put(int.class, Integer::parseInt);
        PARSER_MAP.put(byte.class, Byte::parseByte);
        PARSER_MAP.put(long.class, Long::parseLong);
        PARSER_MAP.put(String.class, String::new);
        PARSER_MAP.put(boolean.class, Boolean::parseBoolean);
        PARSER_MAP.put(float.class, Float::parseFloat);
        PARSER_MAP.put(double.class, Double::parseDouble);
    }

    Map<String, Method> methodMap;
    Map<Method, Pattern> patterns;


    Scanner scanner;

    public void executeNext() {
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }

        String line = scanner.nextLine();
        executeLine(line);
    }

    private void executeLine(String line) {
        String[] splits = line.split(" ");
        if (splits.length > 0) {
            if (methodMap.containsKey(splits[0])) {
                Method m = methodMap.get(splits[0]);
                Pattern p = patterns.get(m);


                Matcher matcher = p.matcher(line);
                Parameter[] parameters = m.getParameters();
                if (matcher.find()) {
                    Object[] args = new Object[m.getParameterCount()];
                    for(int i = 0; i < m.getParameterCount(); i++) {
                        args[i] = PARSER_MAP.get(parameters[i].getType()).Parse(matcher.group("g"+i));
                    }

                    try {
                        m.invoke(this, args);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(getUsage(m));
                }
            } else {
                System.out.println("Unknown command '" + splits[0] + "'.");
            }
        }
    }

    public void close() {
        if (scanner != null) {
            scanner.close();
            scanner = null;
        }
    }

    public Console() {
        methodMap = new HashMap<>();
        patterns = new HashMap<>();
        for(Method m : this.getClass().getDeclaredMethods()) {

            if (m.isAnnotationPresent(Command.class)) {
                String name = m.getAnnotation(Command.class).name();
                if (name.equals("")) {
                    name = m.getName();
                }
                methodMap.put(name, m);
                patterns.put(m, getPattern(m, name));
            }
        }
    }

    private Pattern getPattern(Method m, String name) {
        StringBuilder pattern = new StringBuilder();
        pattern.append(name);
        int i = 0;
        for(Type t : m.getParameterTypes()) {
            pattern.append(" ");
            pattern.append("(?<").append("g"+i++).append(">");
            String p = TYPE_PATTERN_MAP.get(t);
            if (p == null) {
                throw new IllegalArgumentException("Invalid Parameters form Command '" + m.getName()+ "'.");
            }
            pattern.append(p);
            pattern.append(")");
        }
        return Pattern.compile(pattern.toString());
    }

    private static interface Parser{
        Object Parse(String s);
    }

    private String getUsage(Method method) {
        String[] paramNames = method.getAnnotation(Command.class).parameters();

        StringBuilder sb = new StringBuilder()
                .append("Usage: ")
                .append(method.getName());
        int i = 0;
        for(Parameter p : method.getParameters()) {
            if (paramNames.length > i++) {
                sb.append(" ").append(paramNames[i-1]);
            } else {
                sb.append(" ").append(p.getName());
            }
        }
        return sb.toString();
    }
}
