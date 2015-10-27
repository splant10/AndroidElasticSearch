package ca.ualberta.ssrg.movies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import ca.ualberta.ssrg.androidelasticsearch.R;
import ca.ualberta.ssrg.movies.es.ESMovieManager;
import ca.ualberta.ssrg.movies.es.Movie;
import ca.ualberta.ssrg.movies.es.Movies;
import ca.ualberta.ssrg.movies.es.MoviesController;

public class MainActivity extends Activity {

	private ListView movieList;
	private Movies movies;
	private ArrayAdapter<Movie> moviesViewAdapter;
	private ESMovieManager movieManager;
	private MoviesController moviesController;

	private Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		movieList = (ListView) findViewById(R.id.movieList);
	}

	@Override
	protected void onStart() {
		super.onStart();

		movies = new Movies();
		moviesViewAdapter = new ArrayAdapter<Movie>(this, R.layout.list_item,movies);
		movieList.setAdapter(moviesViewAdapter);
		movieManager = new ESMovieManager("*");

		// Show details when click on a movie
		movieList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,	long id) {
				int movieId = movies.get(pos).getId();
				startDetailsActivity(movieId);
			}

		});

		// Delete movie on long click
		movieList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Movie movie = movies.get(position);
				Toast.makeText(mContext, "Deleting " + movie.getTitle(), Toast.LENGTH_LONG).show();

				Thread thread = new DeleteThread(movie.getId());
				thread.start();

				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

        SearchThread thread = new SearchThread("*");

        thread.start();
		
	}
	
	/** 
	 * Called when the model changes
	 */
	public void notifyUpdated() {
		// Thread to update adapter after an operation
		Runnable doUpdateGUIList = new Runnable() {
			public void run() {
				moviesViewAdapter.notifyDataSetChanged();
			}
		};

		runOnUiThread(doUpdateGUIList);
	}

	/** 
	 * Search for movies with a given word(s) in the text view
	 * @param view
	 */
	public void search(View view) {
		movies.clear();

		// TODO: Extract search query from text view
		
		// TODO: Run the search thread


        // http://stackoverflow.com/questions/4531396/get-value-of-a-edit-text-field
        EditText editBox = (EditText) findViewById(R.id.editText1);
        String arg = editBox.getText().toString();
        SearchThread thread = new SearchThread(arg);
        if (arg.equals(null) || arg.equals("")) {
            thread = new SearchThread("*");
        }

        thread.start();
		
	}
	
	/**
	 * Starts activity with details for a movie
	 * @param movieId Movie id
	 */
	public void startDetailsActivity(int movieId) {
		Intent intent = new Intent(mContext, DetailsActivity.class);
		intent.putExtra(DetailsActivity.MOVIE_ID, movieId);

		startActivity(intent);
	}
	
	/**
	 * Starts activity to add a new movie
	 * @param view
	 */
	public void add(View view) {
		Intent intent = new Intent(mContext, AddActivity.class);
		startActivity(intent);
	}


	class SearchThread extends Thread {
		// TODO: Implement search thread
        private String search;

        public SearchThread(String search) {
            this.search = search;
        }

        @Override
        public void run() {
            movies.clear();
            movies.addAll(movieManager.searchMovies(search, null));
            notifyUpdated();

        }
	}

	
	class DeleteThread extends Thread {
		private int movieId;

		public DeleteThread(int movieId) {
			this.movieId = movieId;
		}

		@Override
		public void run() {
			moviesController.deleteMovie(movieId);

			// Remove movie from local list
			for (int i = 0; i < movies.size(); i++) {
				Movie m = movies.get(i);

				if (m.getId() == movieId) {
					movies.remove(m);
					break;
				}
			}
		}
	}
}