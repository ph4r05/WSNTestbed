/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.main;

/**
 *
 * @author ph4r05
 */
public interface DIcontainer {
    public Object getBean(String string) throws BeansException;

    public <T extends Object> T getBean(String string, Class<T> type) throws BeansException;

    public <T extends Object> T getBean(Class<T> type) throws BeansException;

    public Object getBean(String string, Object[] os) throws BeansException;

    public boolean containsBean(String string);

    public boolean isSingleton(String string) throws NoSuchBeanDefinitionException;

    public boolean isPrototype(String string) throws NoSuchBeanDefinitionException;

    public boolean isTypeMatch(String string, Class<?> type) throws NoSuchBeanDefinitionException;

    public Class<?> getType(String string) throws NoSuchBeanDefinitionException;

    public String[] getAliases(String string);
}
