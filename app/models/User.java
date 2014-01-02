package models;

public class User {
	
		private String _id;

		private String email;
		
		private String name;

		
		public User(FacebookUser facebookUser) {
			this._id = facebookUser.id;
			this.name = facebookUser.name;
		}
		
		public String get_id() {
			return _id;
		}

		public void set_id(String _id) {
			this._id = _id;
		}

		public String getEmail() {
			return email;
		}
		
		public String getName() {
			return name;
		}
		
		public void setEmail(String email) {
			this.email = email;
		}
		
		public void setName(String name) {
			this.name = name;	
		}
		

}
