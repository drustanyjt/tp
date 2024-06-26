package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AGE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_IC_NUMBER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SEX;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.Age;
import seedu.address.model.person.Email;
import seedu.address.model.person.IdentityCardNumber;
import seedu.address.model.person.IdentityCardNumberMatchesPredicate;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Sex;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the identity card number used in the displayed person list. \n"
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: IC (must be a valid identity card number) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_IC_NUMBER + "IC_NUMBER] "
            + "[" + PREFIX_AGE + "AGE] "
            + "[" + PREFIX_SEX + "SEX] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] \n"
            + "Example: " + COMMAND_WORD + " T0123456A "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: \n"
            + "Name: %1$s\n"
            + "Phone: %2$s\n"
            + "Email: %3$s\n"
            + "Identity Card Number: %4$s\n"
            + "Age: %5$s\n"
            + "Sex: %6$s\n"
            + "Address: %7$s\n";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in ClinicMate.";
    private static final Logger logger = LogsCenter.getLogger(EditCommand.class);
    private final EditPersonDescriptor editPersonDescriptor;
    private final IdentityCardNumberMatchesPredicate predicate;

    /**
     * @param predicate of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(IdentityCardNumberMatchesPredicate predicate, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(predicate);
        requireNonNull(editPersonDescriptor);

        this.predicate = predicate;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToEdit = model.getPersonIfExists(predicate)
                .orElseThrow(() -> new CommandException(Messages.MESSAGE_NO_MATCHING_IC));
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToEdit, editedPerson);
        logger.info("Edit command has been executed on Person with IC Number: " + predicate);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String successMessage = String.format(MESSAGE_EDIT_PERSON_SUCCESS,
                editedPerson.getName(),
                editedPerson.getPhone(),
                editedPerson.getEmail(),
                editedPerson.getIdentityCardNumber(),
                editedPerson.getAge(),
                editedPerson.getSex(),
                editedPerson.getAddress());

        return new CommandResult(successMessage);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;
        assert editPersonDescriptor != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        IdentityCardNumber updatedIC = editPersonDescriptor.getIC().orElse(personToEdit.getIdentityCardNumber());
        Age updatedAge = editPersonDescriptor.getAge().orElse(personToEdit.getAge());
        Sex updatedSex = editPersonDescriptor.getSex().orElse(personToEdit.getSex());
        Address updatedAddress = editPersonDescriptor.getAddress().orElse(personToEdit.getAddress());

        // Use the same person for existing fields, but copies the object for every thing else
        return new Person(updatedName, updatedPhone, updatedEmail, updatedIC,
                updatedAge, updatedSex, updatedAddress, personToEdit.getNote());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand)) {
            return false;
        }

        EditCommand otherEditCommand = (EditCommand) other;
        return predicate.equals(otherEditCommand.predicate)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Address address;
        private IdentityCardNumber ic;
        private Age age;
        private Sex sex;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setIC(toCopy.ic);
            setAge(toCopy.age);
            setSex(toCopy.sex);
            setAddress(toCopy.address);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, ic, sex, age, address);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }
        public void setIC(IdentityCardNumber ic) {
            this.ic = ic;
        }
        public Optional<IdentityCardNumber> getIC() {
            return Optional.ofNullable(ic);
        }
        public void setAge(Age age) {
            this.age = age;
        }
        public Optional<Age> getAge() {
            return Optional.ofNullable(age);
        }
        public void setSex(Sex sex) {
            this.sex = sex;
        }
        public Optional<Sex> getSex() {
            return Optional.ofNullable(sex);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor)) {
                return false;
            }

            EditPersonDescriptor otherEditPersonDescriptor = (EditPersonDescriptor) other;
            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(ic, otherEditPersonDescriptor.ic)
                    && Objects.equals(age, otherEditPersonDescriptor.age)
                    && Objects.equals(sex, otherEditPersonDescriptor.sex)
                    && Objects.equals(address, otherEditPersonDescriptor.address);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("ic", ic)
                    .add("age", age)
                    .add("sex", sex)
                    .add("address", address)
                    .toString();
        }
    }
}
