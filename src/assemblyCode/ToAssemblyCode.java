package assemblyCode;

import HelperClasses.ObjectModule;
import sample.Main;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ToAssemblyCode
{
    Stack<String> labels = new Stack<>() ;
    Stack<Character> braces = new Stack<>() ;
    List<String> machines = new ArrayList<>() ;
    int count = 0 ;
    List<String> linkedFiles = new ArrayList<>() ;
    public List<String> getMachines(){return machines ;}
    public List<String> convertToAssembly(Main main  , String name , int start) throws Exception
    {
        File file = new File(name) ;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))) ;
        String str = "" ;
        machines.add("START " + start) ;
        int line = 0 , dec = 0 ;
        while((str=br.readLine()) != null)
        {
            line ++ ;
            if(str.trim().startsWith("#include"))
            {
                String link = str.trim().substring(8).trim() ;
                linkedFiles.add(link.replace("\"" , "").replace(";" , "").trim()) ;
                continue;
            }
            if(str.replace(" " ,"").equals("break;"))
            {
                String last ;
                Object[] lls = labels.stream().filter(e -> e.startsWith(".LL")).toArray();
                if(lls.length >= 1)
                {
                    last = (String) lls[lls.length - 1];
                    int l = Integer.parseInt(last.substring(3)) ;
                    machines.add("JMP .LL" + (l+1)) ;
                    continue ;
                }
                else
                {
                    System.out.println("break is not used inside a loop .") ;
                    break ;
                }
            }
            if(str.trim().equals("}"))
            {
                if(labels.peek().startsWith(".LL"))
                    machines.addAll((List<String>) convertEndFor());
                else
                    machines.addAll((List<String>) convertIfEnd()) ;
                continue ;
            }
            else if(str.trim().equals("{"))
                continue ;
            if(!str.trim().endsWith(";") && !str.trim().startsWith("while") && !str.trim().startsWith("if"))
            {
                main.errorOccurred("No semicolon in " + file + " \non line " + line);
                return null ;
            }
            str = str.replace(';' , ' ').trim() ;
            Object res =  convertDeclarative(str) ;
            if(res instanceof Boolean)
            {
                dec = 1 ;
                str = str.replace(" " , "") ;
                res = convertAssignment(str) ;
                if(res instanceof Boolean)
                {
                    res = convertTernary(str) ;
                    if(res instanceof Boolean)
                    {
                        res = convertWhile(str) ;
                        if(res instanceof Boolean)
                        {
                            res = convertIfStart(str) ;
                            if(res instanceof Boolean)
                            {
                                System.out.println("Unrecognizable statement : " + str);
                                main.errorOccurred("Unrecognizable statement \non line " + line + " in file " + name);
                                return null ;
                            }
                            else
                                machines.addAll((List<String>)res) ;
                        }
                        else
                            machines.addAll((List<String>)res) ;
                    }
                    else
                        machines.addAll((List<String>)res) ;
                }
                else
                    machines.addAll((List<String>)res) ;
            }
            else
            {
                if(dec == 0)
                    machines.addAll((List<String>)res) ;
                else
                {
                    System.out.println("All declarations should be at same place .");
                    break ;
                }
            }

        }
        machines.add("END") ;
        machines.stream().forEach(System.out::println);
        File f = new File(name + ".assembly") ;
        PrintWriter pw = new PrintWriter(new FileOutputStream(f) , true) ;
        machines.stream().forEach(pw::println);
        pw.close();
        br.close() ;
        return linkedFiles ;

    }
    public boolean isDeclaration(String str)
    {
        Pattern p = Pattern.compile("^(extern[ ]+)?int[ ]+((\\w+)[ ]*,[ ]*)*(\\w+)[ ]*$") ;
        Matcher matcher = p.matcher(str) ;
        return matcher.matches() ;
    }
    public boolean isStatement(String str)
    {
        Pattern p = Pattern.compile("^(\\w+[-+/*]|\\d+[-+*/])*(\\w+|\\d+)$") ;
        Matcher matcher = p.matcher(str) ;
        return matcher.matches() ;
    }
    public Object convertDeclarative(String str)
    {
        str = str.trim() ;
        str = str.replaceAll("[ ]{2,}" , " ") ;
        if(isDeclaration(str))
        {
            String second = null ;
            String xt = "" ;
            if(!str.startsWith("extern"))
            {
                second = str.substring(3).trim();
            }
            else
            {
                second = str.substring(11).trim();
                xt = " extern" ;
            }
            String[] variables = second.split("[ ]*,[ ]*") ;
            List<String> machines = new ArrayList<>() ;
            for(String variable : variables)
            {
                machines.add("DS " + variable + xt) ;
            }
            return machines ;
        }
        else
            return false ;
    }
    public Object convertAssignment(String str) throws Exception
    {
        String[] parts = str.split("=") ;
        if(parts.length == 2)
        {
            Pattern p = Pattern.compile("\\w+") ;
            if (!(p.matcher(parts[0].trim()).matches() && isStatement(parts[1].replace(';' , ' ').trim())))
                return false ;
            else
            {
                List<String> list = new ArrayList<>() ;
                list.addAll(convertExpression(parts[1].replace(';' , ' ').trim())) ;
                list.add("STORE _Acc " + parts[0]) ;
                return list ;
            }
        }
        else
            return false ;
    }
    public List<String> convertExpression(String expression) throws Exception
    {
        String[] vars = expression.split("[-+*/]") ;
        Set<String> varss = new HashSet<>() ;
        Collections.addAll(varss, vars);
        for(String v : varss)
        {
            expression = expression.replaceAll("\\b" + v + "\\b", v + "\\$");
        }
        if(vars.length == 1)
        {
            try
            {
                List<String> machines = new ArrayList<String>() ;
                machines.add("LOADIM _Acc $" + Integer.parseInt(vars[0])) ;
                return machines ;
            }
            catch(NumberFormatException e)
            {
                List<String> machines = new ArrayList<>();
                machines.add("LOAD _Acc " + vars[0]);
                return machines;
            }
        }
        String postfix = Utilities.infixToPostfix(expression) ;
        List<String> machines = new ArrayList<>() ;
        int index = 0 ;
        int reg_stack = 0 ;
        for(int i = 0 ; i < postfix.length() ; i++)
        {
            if(reg_stack >= 12)
            {
                System.out.println("Out of registers .");
                break ;
            }
            else
            {
                if(postfix.charAt(i) == '+' || postfix.charAt(i) == '-' || postfix.charAt(i) == '*' || postfix.charAt(i) == '/')
                {
                    switch(postfix.charAt(i))
                    {
                        case '+' :
                            machines.add("ADD _helper" + (reg_stack-2) + " _helper" + (reg_stack-1)) ;//helper_1 + helper_2 -> helper_1 ;
                            reg_stack-- ;
                            break ;
                        case '-' :
                            machines.add("SUB _helper" + (reg_stack-2) + " _helper" + (reg_stack-1)) ;
                            reg_stack-- ;
                            break ;
                        case '*' :
                            machines.add("MUL _helper" + (reg_stack-2) + " _helper" + (reg_stack-1)) ;
                            reg_stack-- ;
                            break ;
                        case '/' :
                            machines.add("DIV _helper" + (reg_stack-2) + " _helper" + (reg_stack-1)) ;
                            reg_stack-- ;
                            break ;
                    }
                }
                else
                {
                    String var = "" ;
                    while(postfix.charAt(i) != '$')
                    {
                        var = var + postfix.charAt(i);
                        i++ ;
                    }
                    try
                    {
                        machines.add("LOADIM _helper" + reg_stack + " $" + Integer.parseInt(var));
                    }
                    catch (NumberFormatException e)
                    {
                        machines.add("LOAD _helper" + reg_stack + " " + var);
                    }
                    reg_stack++ ;
                }
            }
        }
        machines.add("MOVE _helper" + (reg_stack - 1) + " _Acc") ;
        return machines ;
    }
    public Object convertCondition(String str) throws Exception
    {
        Pattern p = Pattern.compile("(.*)(==|<|>)(.*)") ;
        Matcher matcher = p.matcher(str) ;
        if(!matcher.matches())
            return false ;
        else
        {
            List<String> machines = new ArrayList<>() ;
            if(isStatement(matcher.group(1)) && isStatement(matcher.group(3)))
            {
                machines.addAll(convertExpression(matcher.group(1))) ;
                machines.add("MOVE _Acc _R2") ;//_Acc -> _R2
                machines.addAll(convertExpression(matcher.group(3))) ;
                machines.add("MOVE _Acc _R3") ;
                machines.add("MOVE _R2 _Acc");
                machines.add("MOVE _R3 _R1");
                switch(matcher.group(2))
                {
                    case "==" :
                        machines.add("CMPEQ") ;
                        break ;
                    case "<" :
                        machines.add("CMPLT") ;
                        break ;
                    case ">" :
                        machines.add("CMPGT") ;
                        break ;
                }
                return machines ;
            }
            else
                return false ;
        }
    }
    public Object convertTernary(String str) throws Exception
    {
        Pattern p = Pattern.compile("^(\\w+)=(.+)\\?(.+):(.+)$") ;
        Matcher matcher = p.matcher(str) ;
        if( !(matcher.matches()))
         return false ;
        else
        {
            String condition = matcher.group(2) ;
            String exp1 = matcher.group(3);
            String exp2 = matcher.group(4) ;
            List<String> machines = new ArrayList<>() ;
            Object conditions = convertCondition(condition) ;
            if(conditions instanceof Boolean)
            {
                return false ;
            }
            else
            {
                machines.addAll((List<String>)conditions) ;
                machines.add("JZERO .LT" + count) ;
                machines.addAll(convertExpression(exp1)) ;
                machines.add("JMP .LT" + (count+1)) ;
                machines.add(".LT" + count) ;
                machines.addAll(convertExpression(exp2)) ;
                machines.add(".LT" + (count+1)) ;
                machines.add("STORE _Acc " + matcher.group(1)) ;
                count += 2 ;
                return machines ;
            }
        }
    }
    public Object convertWhile(String str) throws Exception
    {
        Pattern p = Pattern.compile("^while\\((.*)\\)$") ;
        Matcher matcher = p.matcher(str) ;
        if(matcher.matches())
        {
            Object condition = convertCondition(matcher.group(1));
            if ((condition instanceof Boolean))
                return false ;
            else
            {
                List<String> machines = new ArrayList<>() ;
                String label = ".LL" + count ;
                labels.push(label) ;
                machines.add(label) ;
                machines.addAll((List<String>)condition) ;
                machines.add("JZERO .LL" + (count+1)) ;
                count = count + 2 ;
                return machines ;
            }
        }
        else
            return false ;
    }
    public Object convertEndFor()throws Exception
    {
        List<String> machines = new ArrayList<>() ;
        String label = labels.pop() ;
        machines.add("JMP " + label) ;
        machines.add(".LL" + (Integer.parseInt(label.substring(3))+1)) ;
        return machines ;
    }
    public Object convertIfStart(String str) throws Exception
    {
        Pattern p = Pattern.compile("^if\\((.*)\\)$");
        Matcher matcher = p.matcher(str) ;
        if(matcher.matches())
        {
            Object res = convertCondition(matcher.group(1)) ;
            if(res instanceof Boolean)
                return false ;
            else
            {
                List<String> machines = new ArrayList<>() ;
                machines.addAll((List<String>)res) ;
                labels.add(".LI" + count) ;
                machines.add("JZERO .LI" + count) ;
                count++ ;
                return machines ;
            }
        }
        else
            return false ;
    }
    public Object convertIfEnd()
    {
        if(labels.peek().startsWith(".LLI"))
            return false ;
        else
        {
            List<String> machines = new ArrayList<>() ;
            machines.add(labels.pop()) ;
            return machines ;
        }
    }
}
