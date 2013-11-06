package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexValidatorVerifier implements Verifier<Object> {
    @Override
    public VerifierResult verify(Object obj) {
        String cValidator = "pcrevalidate";
        String notValid = "BAD";

        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();

        Process cPgm = null;
        try {
            //Run the C program
            cPgm = Runtime.getRuntime().exec(cValidator);

            //Open stdin to communicate with the C program
            OutputStream stdin = cPgm.getOutputStream();

            //Open socket to listen to the C program response
            InputStream rd = cPgm.getInputStream();

            //Write regex to C regex validator and close the stream
            stdin.write(obj.toString().getBytes());
            stdin.flush();
            stdin.close();

            //Scan for the response then close the streams
            InputStreamReader reader = new InputStreamReader(rd);
            Scanner scan = new Scanner(reader);
            String resp = scan.nextLine();
            scan.close();
            reader.close();
            rd.close();

            //Kill the C program
            cPgm.destroy();

            String status = resp.split("\\|")[0];

            if (status.equals(notValid)) {
                String msg = resp.split("\\|")[1];
                System.out.println("Regex is invalid: " + msg);
                validationResults.add(new ValidationResult(false, "Must provide a valid regex: " + msg));
                return new VerifierResult(false, validationResults);
            }
        } catch (IOException e) {
            System.out.println("PCRE Validation failed due to IOException: " + e.getMessage());
            jValidate(obj, validationResults);
        }
        return new VerifierResult(true);
    }

    private VerifierResult jValidate(Object obj, List<ValidationResult> validationResults) {
        //If PCRE validation happens to fail fallback to JREGEX :/
        try {
            new jregex.Pattern(obj.toString());
        } catch (jregex.PatternSyntaxException exception) {
            validationResults.add(new ValidationResult(false, "Must provide a valid regex: " + exception.getMessage()));
            return new VerifierResult(false, validationResults);
        }
        return new VerifierResult(true);
    }
}