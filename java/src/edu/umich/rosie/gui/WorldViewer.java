package edu.umich.rosie.gui;

import java.util.*;

import sml.*;
import sml.Agent.RunEventInterface;
import sml.Agent.OutputEventInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;

import edu.umich.rosie.soar.SoarUtil;

public class WorldViewer extends JFrame 
	implements RunEventInterface, OutputEventInterface {

	private JButton connectButton;
	private JButton refreshButton;

	private DefaultTableModel model;
	private JTable table;

    private Kernel kernel = null;
	private Agent agent = null;
	private long runEventCallbackId = -1;

	private StringElement flag = null;
	private long lastRefresh = 0L;
	private final long REFRESH_PERIOD_MS = 250L;

	int counter = 0;

	public WorldViewer() {
		super("Rosie World Viewer");

		setupMenu();
		setupTextPane();

        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
				disconnect();
        	}
     	});
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
    	this.setVisible(true);
	}

	private void setupMenu(){
    	JMenuBar menuBar = new JMenuBar();

    	connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(kernel == null){
					try {
						connect();
						connectButton.setText("Disconnect");
					} catch (IllegalStateException e){ }
				} else {
					disconnect();
					connectButton.setText("Connect");
				}
			}
        });
        menuBar.add(connectButton);

    	refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {

			}
        });
        menuBar.add(refreshButton);

    	this.setJMenuBar(menuBar);
	}

	private void setupTextPane(){
		String[] colNames = { "handle", "category", "conf", "vis", "reach", "grab", "predicates" };
		model = new DefaultTableModel(colNames, 0);

		int[] colSizes = { 100, 100, 25, 25, 25, 25, 400 };
		table = new JTable(model);
		for(int i = 0; i < colSizes.length; i += 1){
			table.getColumnModel().getColumn(i).setPreferredWidth(colSizes[i]);
		}

        JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(new Dimension(800, 600));
		this.getContentPane().add(scroll, BorderLayout.CENTER);
	}

	private void updateTable(Identifier worldId){
		Identifier objsId = SoarUtil.getChildId(worldId, "objects");
		if(objsId == null){
			System.err.println("No objects on world id");
			return;
		}

		Set<Identifier> objIds = SoarUtil.getAllChildIds(objsId, "object");
		Map<String, Identifier> objects = new HashMap<String, Identifier>();
		for(Identifier objId : objIds){
			String handle = SoarUtil.getChildString(objId, "handle");
			objects.put(handle, objId);
		}

		Set<String> handles = new TreeSet<String>(objects.keySet());

		int rowCount = model.getRowCount();
		int row = 0;

		for(String handle : handles){
			// Remove old rows
			while(row < rowCount && !model.getValueAt(row, 0).equals(handle)){
				model.removeRow(row);
				rowCount -= 1;
			}
			if(row == rowCount){
				System.out.println("ADDING ROW: " + handle);
				model.addRow(objectRowData(objects.get(handle)));
				rowCount += 1;
				// add row
			} else {
				updateTableRow(row, objects.get(handle));
				// update row
			}
			row += 1;
		}

		//model.setRowCount(0);
		//for(String handle : objects.keySet()){
		//	System.out.println(handle);
		//	Identifier objId = objects.get(handle);
		//	model.addRow(objectRowData(objId));
		//}
	}

	private Object[] objectRowData(Identifier objId){
		Identifier predsId = SoarUtil.getChildId(objId, "predicates");
		return new Object[]{ 
			SoarUtil.getChildString(objId, "handle"),
			SoarUtil.getChildString(objId, "root-category"),
			isConfirmed(predsId) ? "X" : "",
			isVisible(predsId) ? "X" : "",
			isReachable(predsId) ? "X" : "",
			isGrabbed(predsId) ? "X" : "",
			otherPredicates(predsId)
		};
	}

	private void updateTableRow(int row, Identifier objId){
		Identifier predsId = SoarUtil.getChildId(objId, "predicates");
		model.setValueAt(SoarUtil.getChildString(objId, "root-category"), row, 1);
		model.setValueAt(isConfirmed(predsId) ? "X" : "", row, 2);
		model.setValueAt(isVisible(predsId) ? "X" : "", row, 3);
		model.setValueAt(isReachable(predsId) ? "X" : "", row, 4);
		model.setValueAt(isGrabbed(predsId) ? "X" : "", row, 5);
		model.setValueAt(otherPredicates(predsId), row, 6);
	}

	private static boolean isConfirmed(Identifier predsId){
		final String CONFIRMED = new String("confirmed1");
		return CONFIRMED.equals(SoarUtil.getChildString(predsId, "is-confirmed1"));
	}
	private static boolean isVisible(Identifier predsId){
		final String VISIBLE = new String("visible1");
		return VISIBLE.equals(SoarUtil.getChildString(predsId, "is-visible1"));
	}
	private static boolean isReachable(Identifier predsId){
		final String REACHABLE = new String("reachable1");
		return REACHABLE.equals(SoarUtil.getChildString(predsId, "is-reachable1"));
	}
	private static boolean isGrabbed(Identifier predsId){
		final String GRABBED = new String("grabbed1");
		return GRABBED.equals(SoarUtil.getChildString(predsId, "is-grabbed1"));
	}
	private final static List<String> IGNORE_PREDS = Arrays.asList(
			new String[]{ "category", "is-confirmed1", "is-visible1", "is-reachable1", "is-grabbed1", "volume" });
	private static String otherPredicates(Identifier predsId){
		Set<String> preds = new TreeSet<String>();
        for(int index = 0; index < predsId.GetNumberChildren(); index++){
            WMElement wme = predsId.GetChild(index);
			if(!IGNORE_PREDS.contains(wme.GetAttribute())){
				preds.add(wme.GetValueAsString());
			}
        }
		StringBuilder sb = new StringBuilder();
		for(String p : preds){
			sb.append(p + " ");
		}
		return sb.toString();

	}

	public void connect() throws IllegalStateException {
		int port = Kernel.kDefaultSMLPort;
		kernel = Kernel.CreateRemoteConnection(true, null, port, false);
		if (kernel == null || kernel.IsConnectionClosed()){
			kernel = null;
		    throw new IllegalStateException("CreateRemoteConnection failed");
		}
		System.out.println("Connected to remote kernel");

		agent = kernel.GetAgentByIndex(0);
		if (agent == null){
		   throw new IllegalStateException("Remote Kernel did not have an agent");
		}
		System.out.println("Connected to agent " + agent.GetAgentName());

        runEventCallbackId = agent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
		lastRefresh = 0L;
	}

	public void disconnect(){
		if(this.kernel != null){
			if(flag != null){
				flag.DestroyWME();
				flag = null;
			}
            agent.UnregisterForRunEvent(runEventCallbackId);
			agent = null;
			kernel.Shutdown();
			kernel = null;
		}
	}

	@Override
    public void runEventHandler(int id, Object data, Agent agent, int phase) {
		if(flag == null){
			// Create a flag that tells the agent to copy the world to the output link
			flag = agent.GetInputLink().CreateStringWME("monitor-world", "true");
			agent.Commit();
		}

		counter += 1;
		long now = System.nanoTime();
		if((now - lastRefresh)/1000000 < REFRESH_PERIOD_MS){
			return;
		}

		// Check if the world is on the output link, if so, update
		Identifier outputLink = agent.GetOutputLink();
		if(outputLink != null){
			Identifier worldId = SoarUtil.getChildId(outputLink, "world");
			if(worldId != null){
				try {
				updateTable(worldId);
				} catch (Exception e){
					e.printStackTrace();
				}
				//System.out.println(String.format("#DC/s: %d", counter));
				lastRefresh = now;
				counter = 0;
			}
		}
    }

    @Override
    public void outputEventHandler(Object data, String agentName,
            String attributeName, WMElement wme) {
    	if(!wme.IsJustAdded() || !wme.IsIdentifier()){
    		return;
    	}
    	Identifier id = wme.ConvertToIdentifier();
    	onOutputEvent(attributeName, id);
    }

	private void onOutputEvent(String attName, Identifier id){

	}

    public static void main(String[] args) {
        new WorldViewer();
    }

}
