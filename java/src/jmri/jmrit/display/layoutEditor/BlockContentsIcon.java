package jmri.jmrit.display.layoutEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.swing.JOptionPane;
import jmri.Block;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a Block Object.
 * <p>
 * This is the same name as display.BlockContentsIcon, it follows
 * on from the MemoryIcon
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class BlockContentsIcon extends jmri.jmrit.display.BlockContentsIcon {

    //TODO: unused - dead-code strip
    //private final String defaultText = " ";

    public BlockContentsIcon(String s, LayoutEditor panel) {
        super(s, panel);
        this.panel = panel;
        log.debug("BlockContentsIcon ctor= {}", BlockContentsIcon.class.getName());
    }

    private LayoutBlock lBlock = null;
    LayoutEditor panel;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlock(jmri.NamedBeanHandle<Block> m) {
        super.setBlock(m);
        if (getBlock() != null) {
            lBlock = jmri.InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(getBlock());
        }
    }

    /**
     * add a roster to this icon
     * @param roster to add
     */
    @Override
    protected void addRosterToIcon(RosterEntry roster) {
        if (!jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() || lBlock == null) {
            super.addRosterToIcon(roster);
            return;
        }

        int paths = lBlock.getNumberOfThroughPaths();
        jmri.Block srcBlock = null;
        jmri.Block desBlock = null;
        for (int i = 0; i < paths; i++) {
            if (lBlock.isThroughPathActive(i)) {
                srcBlock = lBlock.getThroughPathSource(i);
                desBlock = lBlock.getThroughPathDestination(i);
                break;
            }
        }
        int dirA;
        int dirB;
        if (srcBlock != null && desBlock != null) {
            dirA = lBlock.getNeighbourDirection(srcBlock);
            dirB = lBlock.getNeighbourDirection(desBlock);
        } else {
            dirA = jmri.Path.EAST;
            dirB = jmri.Path.WEST;
        }

        Object[] options = {"Facing " + jmri.Path.decodeDirection(dirB),
            "Facing " + jmri.Path.decodeDirection(dirA),
            "Do Not Add"};
        int n = JOptionPane.showOptionDialog(this,
                "Would you like to assign loco "
                + roster.titleString() + " to this location",
                "Assign Loco",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);
        if (n == 2) {
            return;
        }
        if (n == 0) {
            flipRosterIcon = true;
            getBlock().setDirection(dirB);
        } else {
            flipRosterIcon = false;
            getBlock().setDirection(dirA);
        }
        if (getBlock().getValue() == roster) {
            //No change in the loco but a change in direction facing might have occurred
            updateIconFromRosterVal(roster);
        } else {
            setValue(roster);
        }
    }

    // force a redisplay when content changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        super.propertyChange(e);
        panel.redrawPanel();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockContentsIcon.class);
}
