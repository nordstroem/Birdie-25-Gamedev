package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JFrame;

import GLEngine.Vector3f;
import entity.VehicleEntity;

public class TrafficInfo {
	public class Node{
		public Vector3f pos = new Vector3f();
		ArrayList<Node> next = new ArrayList<Node>(); // Alla noder som man kan åka till efter denna
		public Node(float x, float z) {
			pos.x = x;
			pos.z = z;
		}
		public Node getNext() {
			if (next.size() == 0) {
				return null;
			} else {
				return next.get((int)(Math.random() * next.size()));
			}
		}
		public ArrayList<Node> getAllNext() {
			return next;
		}
	}

	private ArrayList<VehicleEntity> vehicles = new ArrayList<VehicleEntity>();
	public void addVehicle(VehicleEntity aiVehicle) {
		vehicles.add(aiVehicle);
	}
	
	public ArrayList<VehicleEntity> getCarsWithin(VehicleEntity vehicle, double max){
		ArrayList<VehicleEntity> nearby = new ArrayList<VehicleEntity>();
		Vector3f pos = new Vector3f(vehicle.getCenter().x, vehicle.getCenter().y, vehicle.getCenter().z);
		for (VehicleEntity v : vehicles) {
			if (v != vehicle && new Vector3f(v.getCenter().x, v.getCenter().y, v.getCenter().z).distanceTo(pos) < max) {
				nearby.add(v);
			}
		}
		return nearby;
	}
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private int nextBatchId = 0;
	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public void newNodeBatch() {
		if (batches.size() != 0) {
			for (IntermediateNode in : batches.get(batches.size() - 1).nodes) {
				if (in.prev != -1) {
					batches.get(batches.size() - 1).nodes.get(in.prev).last = false;
				}
			}
		}
		batches.add(new Batch());
	}

	class IntermediateNode{
		float x;
		float z;
		int prev;
		boolean last = true; 
		public IntermediateNode(float x, float z, int prev) {
			this.x = x;
			this.z = z;
			this.prev = prev;
		}
	}
	class Batch{
		ArrayList<IntermediateNode> nodes = new ArrayList<IntermediateNode>();
	}
	ArrayList<Batch> batches = new ArrayList<Batch>();
	public void addIntermediateNode(javax.vecmath.Vector3f pos, int prev) {
		batches.get(batches.size() - 1).nodes.add(new IntermediateNode(pos.x, pos.z, prev));
	}

	
	public void createFinalInfo() {
		for (IntermediateNode in : batches.get(batches.size() - 1).nodes) { // Add the last batch
			if (in.prev != -1) {
				batches.get(batches.size() - 1).nodes.get(in.prev).last = false;
			}
		}
		ArrayList<Boolean> first = new ArrayList<Boolean>();
		ArrayList<Boolean> last = new ArrayList<Boolean>();
		ArrayList<Integer> batchNums = new ArrayList<Integer>();
		int bi = 0;
		for (Batch b : batches) {
			ArrayList<Node> batchNodes = new ArrayList<Node>();
			for (IntermediateNode in : b.nodes) {
				batchNodes.add(new Node(in.x, in.z));
				first.add(in.prev == -1);
				last.add(in.last);
				batchNums.add(bi);
			}
			for (int i = 0; i < b.nodes.size(); i++) { // Fixa next inom tilen
				if (b.nodes.get(i).prev != -1) {
					batchNodes.get(b.nodes.get(i).prev).next.add(batchNodes.get(i)); 
				}
			}
			nodes.addAll(batchNodes);
			bi++;
		}
		
		int i = 0;
		for (Node p : nodes) {
			if (last.get(i)) {
				int j = 0;
				for (Node n : nodes) {
					//double thres = 1;
					if (batchNums.get(i) != batchNums.get(j) && p != n && first.get(j)) {
						if (p.next.isEmpty()) {
							p.next.add(n);
						}else{
							// PRE: Att avslutande noder endast har en next
							double dx = p.pos.x - n.pos.x;
							double dz = p.pos.z - n.pos.z;
							double dxCurrent = p.pos.x - p.next.get(0).pos.x;
							double dzCurrent = p.pos.z - p.next.get(0).pos.z;
							if (dx*dx + dz*dz < dxCurrent*dxCurrent + dzCurrent*dzCurrent) {
								p.next.set(0, n);
							}
						}
					}
					j++;
				}
			}
			i++;
		}
	}
}

