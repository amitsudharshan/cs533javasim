// THIS IS WHAT COMMENTS LOOK LIKE
// BLANK LINES DO NOT MATTER
// Comments only work by having a '//' character as the first chars in a line
// HOWEVER YOU MUST HAVE ONLY 1 space between each TOKEN!

.data
// THIS IS THE DATA SECTION IT MUST COME FIRST
// Data labels start with $ then the lable name and then following a space the 32bit hex value.
$Basketball 0x32
$Baseball 0x16
$Sports 0x0
$NegativeOne 0xFFFFFFFF

.instructions
// THIS IS THE INSTRUCTION SECTION.
// It must come immediately after the data section
// labels are specified using a # charachter. 
// labels cannot immediately follow labels
// you will probably want to start your instruction stream with a label
// otherwise it will be a challenge to access instructions between the start and the first label

 
// below is the instruction stream:

#startOfInstructions
// First load the address for $Baseball into r1
lui r1 U$Baseball
ori r1 r1 L$Baseball
//Now we load the value into r3
lw r1 r3

// Now load the address for $Baseball into r2
lui r2 U$Basketball
ori r2 r2 U$Basketball
//and load the value into r4
lw r2 r4
// now we are going to put the value of M[$baseball] into M[$Basketball]
sw r1 r4
//and then we pull this swapped value into r5 to verify
lw r1 r5
//and now stop the processor
halt

.startpc
//In this section each line indicates the starting label for a given processor
// thus here is where we define number of processors and their starting point
#startOfInstructions
#startOfInstructions
