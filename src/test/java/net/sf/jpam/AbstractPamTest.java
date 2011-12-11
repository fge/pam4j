package net.sf.jpam;

import org.testng.annotations.BeforeClass;

public class AbstractPamTest
{
    protected String user;
    protected String passwd;
    protected String badPasswd;

    @BeforeClass
    public void setUp()
        throws PamException
    {
        user = System.getProperty("test.login");
        passwd = System.getProperty("test.passwd");
        if (user == null || passwd == null)
            throw new IllegalStateException("Please define test.login and"
                + " test.passwd before running tests");
        badPasswd = passwd + "x";
    }
}
