# Step 1

Identify all non-linear variable expressions, and replace each with a new symbolic variable. Each time an expression is killed (i.e. one of its operands is redefined), reassign the symbolic variable. 

For example,

a = b\*b;  
b = b+1;  
a = b\*b;

is rewritten as

int x0 = Debug.makeSymbolicInteger("x0");  
a = x0;  
b = b+1;  
x0 = Debug.makeSymbolicInteger("x0");  
a = x0; 

# Step 2

Use reaching definitions analysis to remove definitions that are not used.            
For example, we can remove the re-assignment of x0 after b = b+1, since it is not used before it is reassigned.

a = b\*c;  
b = b+1;  
c = c+1;  
a = b\*c;

int x0 = Debug.makeSymbolicInteger("x0");  
a = x0;  
b = b+1;  
~~x0 = Debug.makeSymbolicInteger("x0");~~  
c = c+1;  
x0 = Debug.makeSymbolicInteger("x0");  
a = x0; 
