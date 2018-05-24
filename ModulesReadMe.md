# How to add a module

If you want to add a module, you have to put the .java file of the module
in the folder /fwdask/src/main/java/org/onosproject/modules. The module must implements the interface Module.java and so it must implements givesOpinion(...) method.

Eventually you have to edit the file modules.txt in the folder /fwdask/src/main/resources/modules.
You have to add a line under the already existing names that specify the name of your module class.
For exemple if your module is named MyModule.java, you have to add MyModule (without the extension
.java).
