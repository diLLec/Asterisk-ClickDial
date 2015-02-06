package de.neue_phase.asterisk.ClickDial.widgets;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.widgets.Control;


public class ClickDialContentProposalAdapter extends ContentProposalAdapter {

    public ClickDialContentProposalAdapter (Control control,
                                            IControlContentAdapter controlContentAdapter,
                                            IContentProposalProvider proposalProvider, KeyStroke keyStroke,
                                            char[] autoActivationCharacters) {
        super (control, controlContentAdapter, proposalProvider, keyStroke, autoActivationCharacters);
    }

    public void open () {
        this.openProposalPopup ();
    }

    public void close () {
        closeProposalPopup ();
    }
}
