import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class lab2 {
    
    // Hold the actual string of the instruction per line that is identified in the file
    public static ArrayList<String> instructionArray = new ArrayList<String>();

    // Label name and the line numbers that the label is seen on
    public static HashMap<String, Integer> labelToLineMap = new HashMap<String, Integer>();

    // Label name and the line numbers that the label is seen on
    public static HashMap<String, String> labelToInstructionMap = new HashMap<String, String>();

    // 1. Define Instruction Set
    // 2. Create Data Structure (Array)
    // 3. Implement First Pass (run thru all lines of code and compute address of each label)
    // 4. Implement Second Pass (all instruction converted to machine code)
    // 5. Error Handling
    // 6. Output Machine Code
    // 7. Testing 


    //check if empty 
    //check if hashtag
    //check if white space and !$ <= this tells us instruction
        // if next char is : lable!  put in hash
    public static void readAndProcessFile(String fileNameWithPath) throws Exception {

        File file = new File(fileNameWithPath);

        BufferedReader reader = null;

        try {
            // Reader is used to read the file pointer passed to readFile for parsing
            reader = new BufferedReader(new FileReader(file));

            // Declare a string variable representing a line of the line
            String line;

            // Keep track of the number of lines in the files (needed for identiying which line a label is on)
            int lineCount = 0;

            // An array of 32 intergers to hold values of each register
            int[] registerArray = new int[32];

            // first pass find labels 
            // also added instructions to arrayList
            line = reader.readLine();
            while (line != null) {
                line = line.trim();

                // Seperate the string into words (break on space)
                //String[] words = line.split("(?<!#.*),(?=(?:[^#]*#)?[^#]*$)|(\\s+)|(?=#)");
                String[] words = line.split("\\s*,\\s*|\\s+|(?<=\\S)(?=#)|(?<=[a-zA-Z])(?=\\$)");
                // Process each word to identify what to do, this is a nested loop to process the line
                Boolean isComment = false;
                Boolean isLabel = false;
                String finalInstruction = "";
                String finalLabelInstruction = "";
                String finalLabelName = "";
                // String finalComment = ""; // currently unused because we ignore comments

                // test:	add $s0, $s0, $a0 # this is a comment
                // addi $a0, $a0, -1
                // bne $a0, $0, test	# this is another comment
                // beq$t0,$t1,test
                for(int i = 0; i < words.length; ++i) {
                    int indexOfHash = words[i].indexOf("#");
                    int indexOfColon = words[i].indexOf(":");

                    // If we are a comment, at this point we can break and complete the instructions and labels
                    if(isComment) {
                        break;
                    } 

                    // Process the the case of comments
                    if(indexOfHash >= 0 && indexOfColon >= 0) {
                        if(isLabel) {
                            throw new Exception("You cannot define 2 labels on 1 line");
                        }

                        isLabel = true;
                        // We know the string contains a comment (#), now it also contains a label (:).
                        // This means it is either a label and then a comment, a comment that contains a label which can be ignored
                        // or a label, then an instruction, then a comment
                        if(indexOfColon < indexOfHash) {
                            isComment = true;
                            // check if the string contains a $, otherwise we know that the 
                            // TODO: HANDLE THIS IF NEEDED
                                
                            finalLabelName = words[i].substring(0, indexOfColon);
                            finalLabelInstruction += (" " + words[i].substring(indexOfColon+1, indexOfHash));
                        } else {
                            isComment = true;
                            // The hash comes before the label, but that means it could also have a leading instruction before the comment
                            finalInstruction += (" " + words[i].substring(0, indexOfHash));
                        }
                    } else if(indexOfColon >= 0) {
                        // Process the case of a label found, it is similiar to the comment found, but in a different sequence
                        if(isLabel) {
                            throw new Exception("You cannot define 2 labels on 1 line");
                        }

                        isLabel = true;
                        
                        // Identify the label and if necessary start the label instructions
                        finalLabelName = words[i].substring(0, indexOfColon);
                        finalLabelInstruction += (" " + words[i].substring(indexOfColon+1));
                    } else if(indexOfHash >= 0) {
                        isComment = true;
                        // The hash comes before the label, but that means it could also have a leading instruction before the comment
                        finalInstruction += (" " + words[i].substring(0, indexOfHash));
                    } else {
                        // We are in the normal case of a word, the word is either part of a label instruction or part of a normal instruction
                        // we ignore comments, so it can't be part of a comment
                        if(isLabel) {
                            finalLabelInstruction += (" " + words[i]);
                        } else {
                            finalInstruction += (" " + words[i]);
                        }
                    }
                }

                finalLabelName = finalLabelName.trim();
                finalInstruction = finalInstruction.trim();
                finalLabelInstruction = finalLabelInstruction.trim(); 

                if(finalInstruction != null && finalInstruction.length() > 0) {
                    instructionArray.add(finalInstruction);
                }

                if(isLabel) {
                    labelToLineMap.put(finalLabelName, lineCount);
                }

                if(isLabel && finalLabelInstruction.length() > 0) {
                    labelToInstructionMap.put(finalLabelName, finalLabelInstruction);
                    instructionArray.add(finalLabelInstruction);
                }

                // We only want to increment line count if we have an actionable line (ignore blanks and comments)
                if(finalLabelInstruction.length() > 0 || finalInstruction.length() > 0) {
                    lineCount += 1;
                }
                
                // Reset variables
                finalLabelName = "";
                finalLabelInstruction = "";
                finalInstruction = "";
                isComment = false;
                isLabel = false;

                line = reader.readLine();
            }


            // Now that we have populated our array of instructions, labels and ignored comments
            // process the array to output the binary
            for(int i = 0; i < instructionArray.size(); ++i) {
                try {
                    String result = lab2.convertSpaceSeparatedLineToBinaryString(instructionArray.get(i), i);
                    if(result.startsWith(("invalid"))) {                        
                        break;
                    }
                } catch(Exception ex) {
                    System.out.println(ex);
                    break;
                }
            }

        } catch (IOException e){
            System.out.println("Error reading file");
        } catch(Exception ex) {
            System.out.println(ex.toString());
        } 
        finally {
            try {
                if(reader != null) {
                    reader.close();
                } 
            }   catch(IOException e) {
                System.out.println("Error closing file");   
            }
        }

    }


    public static void main(String[] args) {

            try {
                System.out.println(args.length);
                System.out.println(args[0]);
                System.out.println(args[1]);
                System.out.println(args[2]);
                if(args.length < 2) {
                    System.out.println("You need to specify the full path to the input file.");
                    return;
                }
                //readAndProcessFile("test.asm");
                readAndProcessFile(args[1]);
            } catch(Exception ex) {
                System.out.println("Exception occured");
            }

        // if (args.length < 1) {
        //     System.out.println("Error not enough args");
        //     return;
        // } else {
        //     //readAndProcessFile(args[0]);
        //     try {
        //         readAndProcessFile("test.asm");
        //     } catch(Exception ex) {
        //         System.out.println("Exception occured");
        //     }
        // }

        

        // blah switch yay
        //switch () {
            //case 1: //blah
                //break;
            //default: 
                //break;
        //}

    }

    private static void populateRegisterHashMap() {
        /*
        $zero 0 The Constant Value 0 N.A.
        $at 1 Assembler Temporary No
        $v0-$v1 2-3 Values for Function Results
        and Expression Evaluation No
        $a0-$a3 4-7 Arguments No
        $t0-$t7 8-15 Temporaries No
        $s0-$s7 16-23 Saved Temporaries Yes
        $t8-$t9 24-25 Temporaries No
        $k0-$k1 26-27 Reserved for OS Kernel No
        $gp 28 Global Pointer Yes
        $sp 29 Stack Pointer Yes
        $fp 30 Frame Pointer Yes
        $ra 31 
        */

        registerNameToIntegerMap.put("$zero", 0);
        registerNameToIntegerMap.put("$0", 0);
        registerNameToIntegerMap.put("$at", 1);
        registerNameToIntegerMap.put("$v0", 2);
        registerNameToIntegerMap.put("$v1", 3);
        registerNameToIntegerMap.put("$a0", 4);
        registerNameToIntegerMap.put("$a1", 5);
        registerNameToIntegerMap.put("$a2", 6);
        registerNameToIntegerMap.put("$a3", 7);
        registerNameToIntegerMap.put("$t0", 8);
        registerNameToIntegerMap.put("$t1", 9);
        registerNameToIntegerMap.put("$t2", 10);
        registerNameToIntegerMap.put("$t3", 11);
        registerNameToIntegerMap.put("$t4", 12);
        registerNameToIntegerMap.put("$t5", 13);
        registerNameToIntegerMap.put("$t6", 14);
        registerNameToIntegerMap.put("$t7", 15);
        registerNameToIntegerMap.put("$s0", 16);
        registerNameToIntegerMap.put("$s1", 17);
        registerNameToIntegerMap.put("$s2", 18);
        registerNameToIntegerMap.put("$s3", 19);
        registerNameToIntegerMap.put("$s4", 20);
        registerNameToIntegerMap.put("$s5", 21);
        registerNameToIntegerMap.put("$s6", 22);
        registerNameToIntegerMap.put("$s7", 23);
        registerNameToIntegerMap.put("$t8", 24);
        registerNameToIntegerMap.put("$t9", 25);
        registerNameToIntegerMap.put("$k0", 26);
        registerNameToIntegerMap.put("$k1", 27);
        registerNameToIntegerMap.put("$gp", 28);
        registerNameToIntegerMap.put("$sp", 29);
        registerNameToIntegerMap.put("$fp", 30);
        registerNameToIntegerMap.put("$ra", 31);
    }

    public static String convertSpaceSeparatedLineToBinaryString(String line, int currentInstructionIndex) throws Exception {

        populateRegisterHashMap();

        String result = "";

        String[] segments = line.split(" ");
        switch(segments[0]) {
            case "add":
                // Format in line for add is: opcode rd rs rt
                // opcode (6 bits) | rs (5 bits) | rt (5 bits) | rd (5 bits) | shamt (5 bits) | funct (6 bits)

                // opcode (6 bits)
                result += "000000";

                // rs (5 bits)
                if(segments[2].contains("$")) {
                    String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                    rsString = String.format("%5s", rsString).replace(' ', '0');
                    result += " " + rsString;
                } else {
                    String rsString = Integer.toBinaryString(Integer.parseInt(segments[2]));
                    rsString = String.format("%5s", rsString).replace(' ', '0');
                    result += " " + rsString;
                }

                // rt (5 bits)
                if(segments[3].contains("$")) {
                    String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[3]));
                    rtString = String.format("%5s", rtString).replace(' ', '0');
                    result += " " + rtString;
                } else {
                    String rtString = Integer.toBinaryString(Integer.parseInt(segments[3]));
                    rtString = String.format("%5s", rtString).replace(' ', '0');
                    result += " " + rtString;
                }

                // rd (5 bits)
                if(segments[1].contains("$")) {
                    String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                    rtString = String.format("%5s", rtString).replace(' ', '0');
                    result += " " + rtString;
                } else {
                    String rtString = Integer.toBinaryString(Integer.parseInt(segments[1]));
                    rtString = String.format("%5s", rtString).replace(' ', '0');
                    result += " " + rtString;
                }

                // shamt (5 bits) - NOT USED IN add
                result += " 00000";

                // function (6 bits) - add = 100000
                result += " 100000";
                break;

                case "sub":
                    // Format in line for sub is: opcode rd rs rt
                    // opcode (6 bits) | rs (5 bits) | rt (5 bits) | rd (5 bits) | shamt (5 bits) | funct (6 bits)
        
                    // opcode (6 bits)
                    result += "000000";
        
                    // rs (5 bits)
                    if(segments[2].contains("$")) {
                        String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    } else {
                        String rsString = Integer.toBinaryString(Integer.parseInt(segments[2]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    }
        
                    // rt (5 bits)
                    if(segments[3].contains("$")) {
                        String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[3]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    } else {
                        String rtString = Integer.toBinaryString(Integer.parseInt(segments[3]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    }
        
                    // rd (5 bits)
                    if(segments[1].contains("$")) {
                        String rdString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    } else {
                        String rdString = Integer.toBinaryString(Integer.parseInt(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    }
        
                    // shamt (5 bits) - NOT USED IN sub
                    result += " 00000";
        
                    // funct (6 bits) - sub = 100010
                    result += " 100010";
                    break;

                    case "and":
                    // Format in line for and is: opcode rd rs rt
                    // opcode (6 bits) | rs (5 bits) | rt (5 bits) | rd (5 bits) | shamt (5 bits) | funct (6 bits)
                
                    // opcode (6 bits)
                    result += "000000";
                
                    // rs (5 bits)
                    if(segments[2].contains("$")) {
                        String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    } else {
                        String rsString = Integer.toBinaryString(Integer.parseInt(segments[2]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    }
                
                    // rt (5 bits)
                    if(segments[3].contains("$")) {
                        String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[3]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    } else {
                        String rtString = Integer.toBinaryString(Integer.parseInt(segments[3]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    }
                
                    // rd (5 bits)
                    if(segments[1].contains("$")) {
                        String rdString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    } else {
                        String rdString = Integer.toBinaryString(Integer.parseInt(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    }
                
                    // shamt (5 bits) - NOT USED IN and
                    result += " 00000";
                
                    // funct (6 bits) - and = 100100
                    result += " 100100";
                    break;
                
                    case "or":
                    // Format in line for or is: opcode rd rs rt
                    // opcode (6 bits) | rs (5 bits) | rt (5 bits) | rd (5 bits) | shamt (5 bits) | funct (6 bits)
                
                    // opcode (6 bits)
                    result += "000000";
                
                    // rs (5 bits)
                    if(segments[2].contains("$")) {
                        String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    } else {
                        String rsString = Integer.toBinaryString(Integer.parseInt(segments[2]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    }
                
                    // rt (5 bits)
                    if(segments[3].contains("$")) {
                        String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[3]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    } else {
                        String rtString = Integer.toBinaryString(Integer.parseInt(segments[3]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    }
                
                    // rd (5 bits)
                    if(segments[1].contains("$")) {
                        String rdString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    } else {
                        String rdString = Integer.toBinaryString(Integer.parseInt(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    }
                
                    // shamt (5 bits) - NOT USED IN or
                    result += " 00000";
                
                    // funct (6 bits) - or = 100101
                    result += " 100101";
                    break;

                case "slt":
                    // Format in line for slt is: opcode rd rs rt
                    // opcode (6 bits) | rs (5 bits) | rt (5 bits) | rd (5 bits) | shamt (5 bits) | funct (6 bits)

                    // opcode (6 bits)
                    result += "000000";

                    // rs (5 bits)
                    if(segments[2].contains("$")) {
                        String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    } else {
                        String rsString = Integer.toBinaryString(Integer.parseInt(segments[2]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    }

                    // rt (5 bits)
                    if(segments[3].contains("$")) {
                        String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[3]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    } else {
                        String rtString = Integer.toBinaryString(Integer.parseInt(segments[3]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    }

                    // rd (5 bits)
                    if(segments[1].contains("$")) {
                        String rdString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    } else {
                        String rdString = Integer.toBinaryString(Integer.parseInt(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    }

                    // shamt (5 bits) - NOT USED IN slt
                    result += " 00000";

                    // funct (6 bits) - slt = 101010
                    result += " 101010";
                    break;
                case "sll":
                    // Format in line for sll is: opcode rd rt shamt
                    // opcode (6 bits) | rs (5 bits, unused) | rt (5 bits) | rd (5 bits) | shamt (5 bits) | funct (6 bits)

                    // opcode (6 bits)
                    result += "000000";

                    // rs (5 bits) - NOT USED IN sll
                    result += " 00000";

                    // rt (5 bits)
                    if(segments[2].contains("$")) {
                        String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    } else {
                        String rtString = Integer.toBinaryString(Integer.parseInt(segments[2]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    }

                    // rd (5 bits)
                    if(segments[1].contains("$")) {
                        String rdString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    } else {
                        String rdString = Integer.toBinaryString(Integer.parseInt(segments[1]));
                        rdString = String.format("%5s", rdString).replace(' ', '0');
                        result += " " + rdString;
                    }

                    // shamt (5 bits)
                    if(segments[3].matches("\\d+")) { // shamt is a direct number
                        String shamtString = Integer.toBinaryString(Integer.parseInt(segments[3]));
                        shamtString = String.format("%5s", shamtString).replace(' ', '0');
                        result += " " + shamtString;
                    } else {
                        result += " 00000"; // Default shamt if not provided correctly
                    }

                    // funct (6 bits) - sll = 000000
                    result += " 000000";
                    break;

                case "jr":
                    // Format in line for jr is: opcode rs
                    // opcode (6 bits) | rs (5 bits) | rt (5 bits, unused) | rd (5 bits, unused) | shamt (5 bits, unused) | funct (6 bits)
                
                    // opcode (6 bits)
                    result += "000000";
                
                    // rs (5 bits)
                    if(segments[1].contains("$")) {
                        String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    } else {
                        String rsString = Integer.toBinaryString(Integer.parseInt(segments[1]));
                        rsString = String.format("%5s", rsString).replace(' ', '0');
                        result += " " + rsString;
                    }
                
                    // rt (5 bits) - NOT USED IN jr
                    result += " 00000";
                
                    // rd (5 bits) - NOT USED IN jr
                    result += " 00000";
                
                    // shamt (5 bits) - NOT USED IN jr
                    result += " 00000";
                
                    // funct (6 bits) - jr = 001000
                    result += " 001000";
                    break;
            

                    case "addi":
                        // Format in line for addi is: opcode rt rs immediate
                        // opcode (6 bits) | rs (5 bits) | rt (5 bits) | immediate (16 bits)
                    
                        // opcode (6 bits) - addi = 001000
                        result += "001000";
                    
                        // rs (5 bits)
                        if(segments[2].contains("$")) {
                            String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                            rsString = String.format("%5s", rsString).replace(' ', '0');
                            result += " " + rsString;
                        }
                    
                        // rt (5 bits)
                        if(segments[1].contains("$")) {
                            String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                            rtString = String.format("%5s", rtString).replace(' ', '0');
                            result += " " + rtString;
                        }
                    
                        try {
                            // Parsing and formatting the immediate value
                            int immediateValue = Integer.parseInt(segments[3]);
                            String immediateString = Integer.toBinaryString(immediateValue & 0xFFFF);
                            if (immediateString.length() > 16) {
                                immediateString = immediateString.substring(immediateString.length() - 16); // Ensuring it's exactly 16 bits
                            }
                            immediateString = String.format("%16s", immediateString).replace(' ', '0');
                            result += " " + immediateString;
                        } catch (NumberFormatException e) {
                            result += " 0000000000000000"; // Default immediate if parsing fails
                        }
                    
                        break;
                
                    case "beq":
                        // Format in line for beq is: opcode rs rt offset
                        // opcode (6 bits) | rs (5 bits) | rt (5 bits) | offset (16 bits)
                    
                        // opcode (6 bits) - beq = 000100
                        result += "000100";
                    
                        // rs (5 bits)
                        if(segments[1].contains("$")) {
                            String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                            rsString = String.format("%5s", rsString).replace(' ', '0');
                            result += " " + rsString;
                        }
                    
                        // rt (5 bits)
                        if(segments[2].contains("$")) {
                            String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                            rtString = String.format("%5s", rtString).replace(' ', '0');
                            result += " " + rtString;
                        }
                    
                        try {
                            // Calculating and formatting the offset
                            int targetAddress = labelToLineMap.get(segments[3]); // Assuming you have a way to convert label to address
                            int currentAddress = currentInstructionIndex; // This should be the index of this instruction in the instruction array
                            int offsetValue = targetAddress - (currentAddress + 1); // MIPS branch offset is from the next instruction
                            String offsetString = Integer.toBinaryString(offsetValue & 0xFFFF); // Ensure it is only 16 bits
                            if (offsetString.length() > 16) {
                                offsetString = offsetString.substring(offsetString.length() - 16); // Correcting length if necessary
                            }
                            offsetString = String.format("%16s", offsetString).replace(' ', '0'); // Ensure it is padded to 16 bits
                            result += " " + offsetString;
                        }
                        catch (NumberFormatException e) {
                            result += " 0000000000000000"; // Default offset if parsing fails
                        }
                    
                        break;
                
                

                    case "bne":
                        // Format in line for bne is: opcode rs rt offset
                        // opcode (6 bits) | rs (5 bits) | rt (5 bits) | offset (16 bits)
                    
                        // opcode (6 bits) - bne = 000101
                        result += "000101";
                    
                        // rs (5 bits)
                        if(segments[1].contains("$")) {
                            String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                            rsString = String.format("%5s", rsString).replace(' ', '0');
                            result += " " + rsString;
                        }
                    
                        // rt (5 bits)
                        if(segments[2].contains("$")) {
                            String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                            rtString = String.format("%5s", rtString).replace(' ', '0');
                            result += " " + rtString;
                        }
                    
                                            // offset (16 bits)
                    try {
                        // offset (16 bits) calculated from label
                        int targetAddress = labelToLineMap.get(segments[3]); // Assuming you have a way to convert label to address
                        int currentAddress = currentInstructionIndex; // This should be the index of this instruction in the instruction array
                        int offsetValue = targetAddress - (currentAddress + 1); // MIPS branch offset is from the next instruction
                        String offsetString = Integer.toBinaryString(0x10000 | (offsetValue & 0xFFFF)).substring(1);
                        result += " " + offsetString;
                    }
                    catch (NumberFormatException e) {
                        result += " 0000000000000000"; // Default offset if parsing fails
                    }
                    
                    break;
                
                // case "bne":
                //     // Format in line for bne is: opcode rs rt offset
                //     // opcode (6 bits) | rs (5 bits) | rt (5 bits) | offset (16 bits)
                
                //     // opcode (6 bits) - bne = 000101
                //     result += "000101";
                
                //     // rs (5 bits)
                //     if(segments[2].contains("$")) {
                //         String rsString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[2]));
                //         rsString = String.format("%5s", rsString).replace(' ', '0');
                //         result += " " + rsString;
                //     } else {
                //         String rsString = Integer.toBinaryString(Integer.parseInt(segments[2]));
                //         rsString = String.format("%5s", rsString).replace(' ', '0');
                //         result += " " + rsString;
                //     }
                
                //     // rt (5 bits)
                //     if(segments[3].contains("$")) {
                //         String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[3]));
                //         rtString = String.format("%5s", rtString).replace(' ', '0');
                //         result += " " + rtString;
                //     } else if( (segments[3].charAt(0) >= 'A' && segments[3].charAt(0) <= 'Z') || ( segments[3].charAt(0) >= 'a' && segments[3].charAt(0) <= 'z') ) {
                //         // it's not a number, so it could be a label
                //         String rtString = Integer.toBinaryString(0x10000 | lab2.labelToLineMap.get(segments[3]));
                //         rtString = String.format("%5s", rtString).replace(' ', '0');
                //         result += " " + rtString;
                //     }
                //     else {
                //         String rtString = Integer.toBinaryString(Integer.parseInt(segments[3]));
                //         rtString = String.format("%5s", rtString).replace(' ', '0');
                //         result += " " + rtString;
                //     }
                
                //     // offset (16 bits)
                //     try {
                //         int offsetValue = Integer.parseInt(segments[4]);
                //         String offsetString = Integer.toBinaryString(0x10000 | offsetValue).substring(1);
                //         result += " " + offsetString;
                //     } catch (NumberFormatException e) {
                //         result += " 0000000000000000"; // Default offset if parsing fails
                //     }
                
                //     break;
 
                case "lw":
                    // Format in line for lw is: opcode base rt offset
                    // opcode (6 bits) | base (5 bits) | rt (5 bits) | offset (16 bits)
                
                    // opcode (6 bits) - lw = 100011
                    result += "100011";
                
                    // Extracting the base register and offset from segments[2], which is in the format 'offset($base)'
                    String lwOffsetPart = segments[2].substring(0, segments[2].indexOf('(')).trim();
                    String lwBasePart = segments[2].substring(segments[2].indexOf('$'), segments[2].indexOf(')'));
                
                    // base (5 bits)
                    if (registerNameToIntegerMap.containsKey(lwBasePart)) {
                        String baseString = Integer.toBinaryString(registerNameToIntegerMap.get(lwBasePart));
                        baseString = String.format("%5s", baseString).replace(' ', '0');
                        result += " " + baseString;
                    } else {
                        result += " 00000"; // Default base if not found or incorrect parsing
                    }
                
                    // rt (5 bits)
                    if(segments[1].contains("$")) {
                        String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    } else {
                        result += " 00000"; // Default rt if not properly formatted
                    }
                
                    try {
                        // Calculating and formatting the offset
                        int offsetValue = Integer.parseInt(lwOffsetPart);
                        String offsetString = Integer.toBinaryString(0xFFFF & offsetValue);
                        offsetString = String.format("%16s", offsetString).replace(' ', '0'); // Ensure it is padded to 16 bits
                        result += " " + offsetString;
                    } catch (NumberFormatException e) {
                        result += " 0000000000000000"; // Default offset if parsing fails
                    }
                
                    break;
            
                        
                case "sw":
                    // Format in line for sw is: opcode base rt offset
                    // opcode (6 bits) | base (5 bits) | rt (5 bits) | offset (16 bits)
                
                    // opcode (6 bits) - sw = 101011
                    result += "101011";
                
                    // Extracting the base register and offset from segments[2], which is in the format 'offset($base)'
                    String swOffsetPart = segments[2].substring(0, segments[2].indexOf('(')).trim();
                    String swBasePart = segments[2].substring(segments[2].indexOf('$'), segments[2].indexOf(')'));
                
                    // base (5 bits)
                    if (registerNameToIntegerMap.containsKey(swBasePart)) {
                        String baseString = Integer.toBinaryString(registerNameToIntegerMap.get(swBasePart));
                        baseString = String.format("%5s", baseString).replace(' ', '0');
                        result += " " + baseString;
                    } else {
                        result += " 00000"; // Default base if not found or incorrect parsing
                    }
                
                    // rt (5 bits)
                    if(segments[1].contains("$")) {
                        String rtString = Integer.toBinaryString(registerNameToIntegerMap.get(segments[1]));
                        rtString = String.format("%5s", rtString).replace(' ', '0');
                        result += " " + rtString;
                    } else {
                        result += " 00000"; // Default rt if not properly formatted
                    }
                
                    try {
                        // Calculating and formatting the offset
                        int offsetValue = Integer.parseInt(swOffsetPart);
                        String offsetString = Integer.toBinaryString(0xFFFF & offsetValue);
                        offsetString = String.format("%16s", offsetString).replace(' ', '0'); // Ensure it is padded to 16 bits
                        result += " " + offsetString;
                    } catch (NumberFormatException e) {
                        result += " 0000000000000000"; // Default offset if parsing fails
                    }
                
                    break;
                

                    case "j":
                        // Format for the j instruction is: opcode address
                        // opcode (6 bits) - j = 000010
                        result += "000010";
                    
                        // Assuming segments[1] is the label name, e.g., 'test1'
                        String jLabel = segments[1];  // This should be a label like 'test1'
                    
                        // Retrieve the address from the map
                        Integer labelAddress = labelToLineMap.get(jLabel);
                    
                        if (labelAddress == null) {
                            // If no address found, handle as an error or set a default
                            System.err.println("Label '" + jLabel + "' not found.");
                            result += " " + "00000000000000000000000000"; // Default to zero address if label not found
                        } else {
                            // Convert the address to a 26-bit binary string
                            String addressString = Integer.toBinaryString(labelAddress & 0x03FFFFFF); // Mask to ensure it's within 26 bits
                            addressString = String.format("%26s", addressString).replace(' ', '0'); // Pad with zeros to ensure it's exactly 26 bits
                            result += " " + addressString;
                        }
                    
                        break;
                
                
                case "jal":
                    // Format in line for jal is: opcode address
                    // opcode (6 bits) - jal = 000011
                    result += "000011";
                
                    // Address (26 bits)
                    // segment[1] contains the label 
                    String labelName = segments[1];  
                    int address;
                    if (labelToLineMap.containsKey(labelName)) {
                        address = labelToLineMap.get(labelName);
                    } else {
                        try {
                            address = Integer.parseInt(labelName);  // Direct address handling
                        } catch (NumberFormatException e) {
                            address = 0; // Default address if parsing fails or label not found
                        }
                    }
                
                    // Convert the address to a 26-bit binary string
                    String addressString = Integer.toBinaryString(address & 0x03FFFFFF); // Mask to ensure it's within 26 bits
                    addressString = String.format("%26s", addressString).replace(' ', '0'); // Pad with zeros if necessary
                    result += " " + addressString;
                
                    break;
                
                
                default:
                    result = "invalid instruction: " + segments[0];
                    break;
        }
        
        System.out.println(result);
        
        return result;
    }

        /*
    $zero 0 The Constant Value 0 N.A.
    $at 1 Assembler Temporary No
    $v0-$v1 2-3 Values for Function Results
    and Expression Evaluation No
    $a0-$a3 4-7 Arguments No
    $t0-$t7 8-15 Temporaries No
    $s0-$s7 16-23 Saved Temporaries Yes
    $t8-$t9 24-25 Temporaries No
    $k0-$k1 26-27 Reserved for OS Kernel No
    $gp 28 Global Pointer Yes
    $sp 29 Stack Pointer Yes
    $fp 30 Frame Pointer Yes
    $ra 31 
    */

    public static HashMap<String, Integer> registerNameToIntegerMap = new HashMap<String, Integer>();
}


