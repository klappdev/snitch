package org.kl.handle;

import org.kl.bean.Instruction;
import org.kl.error.ContractException;

import java.util.ArrayList;
import java.util.List;

public class ContractParser {

    private ContractParser() throws ContractException {
        throw new ContractException("Can't create object");
    }

    public static List<Instruction> parseLine(String line) throws ContractException {
        List<Instruction> list = new ArrayList<>();

        if (line.contains("||") || line.contains("&&")) {
            String[] parts = line.trim().replaceAll(" +", " ").split("(\\|\\|)|(&&)");

            for (String part : parts) {
                String[] pieces = part.trim().split(" ");

                if (pieces.length != 3) {
                    throw new ContractException("Correct instruction: left operator right");
                }

                list.add(new Instruction(pieces[0], pieces[1], pieces[2]));
            }
        } else {
            String[] pieces = line.trim().replaceAll(" +", " ").split(" ");

            if (pieces.length != 3) {
                throw new ContractException("Correct instruction: left operator right");
            }

            list.add(new Instruction(pieces[0], pieces[1], pieces[2]));
        }

        return list;
    }

    public static List<Instruction> parseLineRoutine(String line) throws ContractException {
        List<Instruction> list = new ArrayList<>();

        if (!line.endsWith("()")) {
            throw new ContractException("Call method object must end with ()");
        }

        if (!line.contains(".")) {
            throw new ContractException("After name object must follow dot");
        }

        String[] parts = line.split("\\.");

        if (parts.length != 2) {
            throw new ContractException("Correct call method: object.method()");
        }

        list.add(new Instruction(parts[0], ".", parts[1].substring(0, parts[1].length() - 2)));

        return list;
    }
}
