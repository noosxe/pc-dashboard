{
  description = "PC Dashboard Android App";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    android-nixpkgs.url = "github:tadfisher/android-nixpkgs";
    android-nixpkgs.inputs.nixpkgs.follows = "nixpkgs";
  };

  outputs =
    {
      self,
      nixpkgs,
      android-nixpkgs,
    }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
        config.allowUnfree = true;
      };

      sdk = android-nixpkgs.sdk.${system} (
        sdkPkgs: with sdkPkgs; [
          cmdline-tools-latest
          build-tools-37-0-0
          build-tools-36-0-0
          platform-tools
          platforms-android-37-0
          emulator
        ]
      );
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        buildInputs = [
          sdk
          pkgs.gnumake
          pkgs.openjdk17
        ];

        shellHook = ''
          # 1. Create a local .android directory structure if it doesn't exist
          mkdir -p .android

          # 2. Force symlink the current Nix store SDK path to our local directory
          ln -sfn ${sdk}/share/android-sdk .android/sdk

          # 3. Export environment variables for tools running within this shell context
          export ANDROID_HOME=$PWD/.android/sdk
          export ANDROID_USER_HOME=$PWD/.android

          echo "🤖 Android development environment loaded!"
          echo "SDK path symlinked to: $ANDROID_HOME"
        '';
      };
    };
}
