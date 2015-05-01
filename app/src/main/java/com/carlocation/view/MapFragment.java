package com.carlocation.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.carlocation.R;
import com.supermap.data.Environment;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.mapping.MapControl;
import com.supermap.mapping.MapView;
import com.supermap.mapping.dyn.DynamicView;

public class MapFragment extends Fragment {

	private MapView m_mapview = null;
	private Workspace m_workspace = null;
	private MapControl m_mapControl = null;

	private DynamicView m_dynamicLayer = null;

	private String mapPath;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapPath = android.os.Environment.getExternalStorageDirectory()
				.getAbsolutePath().toString()
				+ "/carlocation_map/";
		Environment.setLicensePath(mapPath);
		Environment.initialization(getActivity());
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		 View rootView = inflater.inflate(R.layout.map_fragment, container,
                 false);
		 m_mapview = (MapView) rootView.findViewById(R.id.mapview);
		m_mapControl = m_mapview.getMapControl();
		 openWorkspace();
         return rootView;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	private void openWorkspace() {
		m_mapControl = m_mapview.getMapControl();

		m_workspace = new Workspace();
		WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
		info.setServer(mapPath + "/ShenyangWGS84.smwu");

		info.setType(WorkspaceType.SMWU);
		boolean isOpen = m_workspace.open(info);
		

		m_mapControl.getMap().setWorkspace(m_workspace);
		m_mapControl.getMap().open(m_workspace.getMaps().get(0));
		
		m_dynamicLayer = new DynamicView(getActivity(), m_mapControl.getMap());
		m_mapview.addDynamicView(m_dynamicLayer);
	}

}
