package duke;

import java.time.format.DateTimeParseException;

public class Parser {
    public static Task decodeTask(String line) {
        String[] parts = line.split(" \\| ");
        switch (parts[0]) {
            case "T":
                return new Todo(parts[2], Boolean.parseBoolean(parts[1]));
            case "D":
                return new Deadline(parts[2], Boolean.parseBoolean(parts[1]), parts[3]);
            case "E":
                String[] timeParts = parts[3].split("-");
                return new Event(parts[2], Boolean.parseBoolean(parts[1]), timeParts[0], timeParts[1]);
            default:
                throw new IllegalArgumentException("Unknown task type: " + parts[0]);
        }
    }

    public static void parse(String userInput, TaskList taskList, Storage storage) throws ChatbotException {
        if (userInput.trim().toLowerCase().startsWith("todo")) {
            handleTodoCommand(userInput, taskList, storage);
        } else if (userInput.trim().toLowerCase().startsWith("deadline")) {
            handleDeadlineCommand(userInput, taskList, storage);
        } else if (userInput.trim().toLowerCase().startsWith("event")) {
            handleEventCommand(userInput, taskList, storage);
        } else if ("list".equalsIgnoreCase(userInput)) {
            taskList.printTasks();
        } else if (userInput.trim().toLowerCase().startsWith("mark")) {
            handleMarkCommand(userInput, taskList, storage);
        } else if (userInput.trim().toLowerCase().startsWith("unmark")) {
            handleUnmarkCommand(userInput, taskList, storage);
        } else if (userInput.trim().toLowerCase().startsWith("delete")) {
            handleDeleteCommand(userInput, taskList, storage);
        } else {
            throw new ChatbotException("OOPS!!! I'm sorry, but I don't know what that means :-(");
        }
    }

    private static void handleTodoCommand(String userInput, TaskList taskList, Storage storage) throws ChatbotException {
        String taskDetails = userInput.substring("todo".length()).trim();
        if (taskDetails.isEmpty()) {
            throw new ChatbotException("The description of a todo cannot be empty.");
        }
        Task todo = new Todo(taskDetails, false);
        taskList.addTask(todo, storage);
    }

    private static void handleDeadlineCommand(String userInput, TaskList taskList, Storage storage) throws ChatbotException {
        String taskDetails = userInput.substring("deadline".length()).trim();
        String[] details = taskDetails.split(" /by ", 2);
        if (details.length < 2 || details[0].isEmpty() || details[1].isEmpty()) {
            throw new ChatbotException("The description of a deadline or its due time cannot be empty.");
        }
        try {
            Task deadline = new Deadline(details[0], false, details[1]);
            taskList.addTask(deadline, storage);
        } catch (DateTimeParseException e) {
            throw new ChatbotException("Please use the format dd/MM/yyyy HHmm for dates.");
        }
    }

    private static void handleEventCommand(String userInput, TaskList taskList, Storage storage) throws ChatbotException {
        String taskDetails = userInput.substring("event".length()).trim();
        String[] details = taskDetails.split(" /from ", 2);
        if (details.length < 2 || details[0].isEmpty()) {
            throw new ChatbotException("The description of an event cannot be empty.");
        }
        String[] times = details[1].split(" /to ", 2);
        if (times.length < 2 || times[0].isEmpty() || times[1].isEmpty()) {
            throw new ChatbotException("The start or end time of an event cannot be empty.");
        }
        try {
            Task event = new Event(details[0], false, times[0], times[1]);
            taskList.addTask(event, storage);
        } catch (DateTimeParseException e) {
            throw new ChatbotException("Please use the format dd/MM/yyyy HHmm for dates.");
        }
    }

    private static void handleMarkCommand(String userInput, TaskList taskList, Storage storage) throws ChatbotException {
        int taskNumber = extractTaskNumber(userInput, "mark");
        taskList.markTask(taskNumber, true, storage);
    }

    private static void handleUnmarkCommand(String userInput, TaskList taskList, Storage storage) throws ChatbotException {
        int taskNumber = extractTaskNumber(userInput, "unmark");
        taskList.markTask(taskNumber, false, storage);
    }

    private static void handleDeleteCommand(String userInput, TaskList taskList, Storage storage) throws ChatbotException {
        int taskNumber = extractTaskNumber(userInput, "delete");
        taskList.deleteTask(taskNumber, storage);
    }

    private static int extractTaskNumber(String userInput, String command) throws ChatbotException {
        try {
            return Integer.parseInt(userInput.substring(command.length()).trim());
        } catch (NumberFormatException e) {
            throw new ChatbotException("Invalid task number.");
        }
    }

}
