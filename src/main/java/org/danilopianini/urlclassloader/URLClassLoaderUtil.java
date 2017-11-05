package org.danilopianini.urlclassloader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Utility to manipulate classpath. The implementation relies heavily on
 * reflection, makes access to non-public APIs, and as such must be used only if
 * no better solutions are available. This library may work only on a subset of
 * Java Runtimes. Newer Java runtimes may break its functionality.
 */
public final class URLClassLoaderUtil {

    private URLClassLoaderUtil() {
    }

    /**
     * Adds the provided URL as first entry in the provided classloader.
     * 
     * @param url
     *            the url
     * @param cl
     *            the target classloader
     */
    public static void addFirst(final URL url, final ClassLoader cl) {
        doOn(new OpOnLists() {
            @Override
            protected void doOnList(final List<URL> urlList) {
                urlList.add(0, url);
            }
        }, cl);
    }

    /**
     * Adds the provided URL as first entry in {@link ClassLoader#getSystemClassLoader()}.
     * 
     * @param url
     *            the url
     */
    public static void addFirst(final URL url) {
        addFirst(url, ClassLoader.getSystemClassLoader());
    }

    /**
     * Adds the provided URI as first entry in the provided classloader.
     * 
     * @param uri
     *            the url
     * @param cl
     *            the target classloader
     * @throws IllegalStateException
     *             if the URI can not be translated to a valid URL
     */
    public static void addFirst(final URI uri, final ClassLoader cl) {
        addFirst(toURL(uri), cl);
    }

    /**
     * Adds the provided URI as first entry in {@link ClassLoader#getSystemClassLoader()}.
     * 
     * @param uri
     *            the url
     * @throws IllegalStateException
     *             if the URI can not be translated to a valid URL
     */
    public static void addFirst(final URI uri) {
        addFirst(toURL(uri));
    }

    /**
     * Adds the provided URL as first entry in the provided classloader.
     * 
     * @param url
     *            the url
     * @param cl
     *            the target classloader
     * @throws IllegalStateException
     *             if the String can not be translated to a valid URL
     */
    public static void addFirst(final String url, final ClassLoader cl) {
        addFirst(toURL(url), cl);
    }

    /**
     * Adds the provided URL as first entry in {@link ClassLoader#getSystemClassLoader()}.
     * 
     * @param url
     *            the url
     * @throws IllegalStateException
     *             if the String can not be translated to a valid URL
     */
    public static void addFirst(final String url) {
        addFirst(toURL(url));
    }

    /**
     * Adds the provided File as first entry in the provided classloader.
     * 
     * @param file
     *            the url
     * @param cl
     *            the target classloader
     */
    public static void addFirst(final File file, final ClassLoader cl) {
        addFirst(toURL(file), cl);
    }

    /**
     * Adds the provided File as first entry in {@link ClassLoader#getSystemClassLoader()}.
     * 
     * @param file
     *            the {@link URL}
     */
    public static void addFirst(final File file) {
        addFirst(toURL(file));
    }

    /**
     * Adds the provided URL as last entry in the provided {@link ClassLoader}.
     * 
     * @param url
     *            the {@link URL}
     * @param cl
     *            the {@link ClassLoader}
     */
    public static void addLast(final URL url, final ClassLoader cl) {
        final AccessibleClassLoader ac = new AccessibleClassLoader(cl);
        ac.addURL(url);
    }

    /**
     * Adds the provided File as last entry in {@link ClassLoader#getSystemClassLoader()}.
     * 
     * @param url
     *            the {@link URL}
     */
    public static void addLast(final URL url) {
        addLast(url, ClassLoader.getSystemClassLoader());
    }

    /**
     * Adds the provided File as last entry in the provided {@link ClassLoader}.
     * 
     * @param uri
     *            the {@link URI}
     * @param cl
     *            the {@link ClassLoader}
     */
    public static void addLast(final URI uri, final ClassLoader cl) {
        addLast(toURL(uri), cl);
    }

    /**
     * Adds the provided File as last entry in {@link ClassLoader#getSystemClassLoader()}.
     * 
     * @param uri
     *            the {@link URI}
     */
    public static void addLast(final URI uri) {
        addLast(toURL(uri));
    }

    /**
     * @param url
     *            the {@link URL}
     * @param cl
     *            the {@link ClassLoader}
     */
    public static void addLast(final String url, final ClassLoader cl) {
        addLast(toURL(url), cl);
    }

    /**
     * @param url
     *            the {@link URL}
     */
    public static void addLast(final String url) {
        addLast(toURL(url));
    }

    /**
     * @param file
     *            the url
     * @param cl
     *            the target classloader
     */
    public static void addLast(final File file, final ClassLoader cl) {
        addLast(toURL(file), cl);
    }

    /**
     * @param file
     *            the url
     */
    public static void addLast(final File file) {
        addLast(toURL(file));
    }

    /**
     * @param url
     *            the {@link URL}
     * @param cl
     *            the {@link ClassLoader}
     */
    public static void remove(final URL url, final ClassLoader cl) {
        doOn(new OpOnLists() {
            @Override
            protected void doOnList(final List<URL> urlList) {
                urlList.remove(url);
            }
        }, cl);
        /*doOn(new Op() {
            @Override
            public void run(final URLClassPath cl) {
                try {
                    final Field loadersField = cl.getClass().getDeclaredField("loaders");
                    final Field lmapField = cl.getClass().getDeclaredField("lmap");
                    loadersField.setAccessible(true);
                    lmapField.setAccessible(true);
                    final List<?> loaders = (List<?>) loadersField.get(cl);
                    final Map<?, ?> lmap = (Map<?, ?>) lmapField.get(cl);
                    final Iterator<?> it = loaders.iterator();
                    Method target = null;
                    while (it.hasNext()) {
                        final Object loader = it.next();
                        for (Class<?> loaderClass = loader.getClass(); target == null && !Object.class.equals(loaderClass); loaderClass = loaderClass.getSuperclass()) {
                            final Method[] methods = loaderClass.getDeclaredMethods();
                            int i;
                            for (i = 0; i < methods.length; i++) {
                                final Method m = methods[i];
                                if (m.getName().equals("getBaseURL") && m.getParameterTypes().length == 0) {
                                    target = m;
                                    break;
                                }
                            }
                        }
                        if (target == null) {
                            throw new IllegalStateException("Could not find any getBaseURL() method");
                        }
                        target.setAccessible(true);
                        if (target.invoke(loader).equals(url)) {
                            it.remove();
                            lmap.values().remove(loader);
                        }
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            }
        }, cl);*/
    }

    /**
     * @param url
     *            the {@link URL}
     */
    public static void remove(final URL url) {
        remove(url, ClassLoader.getSystemClassLoader());
    }

    /**
     * @param uri
     *            the {@link URI}
     * @param cl
     *            the {@link ClassLoader}
     */
    public static void remove(final URI uri, final ClassLoader cl) {
        remove(toURL(uri), cl);
    }

    /**
     * @param uri
     *            the {@link URI}
     */
    public static void remove(final URI uri) {
        remove(toURL(uri));
    }

    /**
     * @param url
     *            the {@link URL}
     * @param cl
     *            the {@link ClassLoader}
     */
    public static void remove(final String url, final ClassLoader cl) {
        remove(toURL(url), cl);
    }

    /**
     * @param url
     *            the {@link URL}
     */
    public static void remove(final String url) {
        remove(toURL(url));
    }

    /**
     * @param file
     *            the url
     * @param cl
     *            the target classloader
     */
    public static void remove(final File file, final ClassLoader cl) {
        remove(toURL(file), cl);
    }

    /**
     * @param file
     *            the url
     */
    public static void remove(final File file) {
        remove(toURL(file));
    }

    private static void doOn(final Op operation, final ClassLoader loader) {
        operation.run(new URLClassLoader(new URL[0], loader));
    }

    private static URL toURL(final URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static URL toURL(final String s) {
        try {
            return toURL(URI.create(s));
        } catch (IllegalArgumentException e) {
            return toURL(new File(s));
        }
    }

    private static URL toURL(final File file) {
        return toURL(file.toURI());
    }
    
    private static class AccessibleClassLoader extends URLClassLoader {

        public AccessibleClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }
        
        public void addURL(URL url) {
            super.addURL(url);
        }
        
    }

    private abstract static class OpOnLists extends Op {
        @Override
        public final void run(final URLClassLoader cl) {
            Field cp;
            try {
                cp = cl.getClass().getField("ucp");
                cp.setAccessible(true);
                Object classpath = cp.get(cl);
                Field urls = classpath.getClass().getField("path");
                final List<?> theList = (List<?>) urls.get(classpath);
                if (theList.isEmpty() || theList.get(0) instanceof URL) {
                    /*
                     * This is most likely one of our targets
                     */
                    @SuppressWarnings("unchecked")
                    final List<URL> urlList = (List<URL>) theList;
                    doOnList(urlList);
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        protected abstract void doOnList(List<URL> urlList);
    }

    private abstract static class Op {
        public abstract void run(java.net.URLClassLoader l);
    }

}