package assemblyCode;

import java.util.Stack;

public class Utilities
{
    public static String infixToPostfix(String infixBuffer)
    {
        int priority = 0;
        String postfixBuffer = "";

        Stack<Character> s1 = new Stack<>();

        for (int i = 0; i < infixBuffer.length(); i++)
        {
            char ch = infixBuffer.charAt(i);
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/')
            {
                // check the precedence
                if (s1.size() <= 0)
                    s1.push(ch);
                else
                {
                    Character chTop = (Character) s1.peek();
                    if (chTop == '*' || chTop == '/')
                        priority = 1;
                    else
                        priority = 0;
                    if (priority == 1)
                    {
                        if (ch == '+' || ch == '-')
                        {
                            postfixBuffer += s1.pop();
                            i--;
                        }
                        else
                        { // Same
                            postfixBuffer += s1.pop();
                            i--;
                        }
                    }
                    else
                    {
                        if (ch == '+' || ch == '-')
                        {
                            postfixBuffer += s1.pop();
                            s1.push(ch);
                        }
                        else
                            s1.push(ch);
                    }
                }
            }
            else
            {
                postfixBuffer += ch;
            }
        }
        int len = s1.size();
        for (int j = 0; j < len; j++)
            postfixBuffer += s1.pop();
        return postfixBuffer;
    }
}
