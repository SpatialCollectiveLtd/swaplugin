# Contributing to DPW Mapper Plugin

Thank you for your interest in contributing! This document provides guidelines for contributing to the project.

## Ways to Contribute

### 1. Report Bugs
- Use GitHub Issues
- Include steps to reproduce
- Provide JOSM version and plugin version
- Include error logs if available

### 2. Suggest Features
- Open a GitHub Issue with the "enhancement" label
- Describe the use case
- Explain how it would help mappers

### 3. Improve Documentation
- Fix typos or unclear instructions
- Add examples
- Translate to other languages

### 4. Write Code
- Fix bugs
- Implement new features
- Improve performance
- Add tests

## Development Setup

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Gradle
- Git
- JOSM (for testing)
- IDE (IntelliJ IDEA or Eclipse recommended)

### Clone and Build
```bash
git clone https://github.com/SpatialCollectiveLtd/swaplugin.git
cd swaplugin
./gradlew build
```

### Project Structure
```
swaplugin/
├── src/main/java/org/openstreetmap/josm/plugins/dpwmapper/
│   ├── DPWMapperPlugin.java       # Main plugin class
│   ├── AutoHideListener.java      # Filter auto-activation
│   └── MergeAndFixAction.java     # Merge algorithm
├── src/main/resources/
│   └── dpwmapper.properties        # Plugin metadata
├── images/
│   └── dpwmapper.svg               # Plugin icon
├── build.gradle                    # Build configuration
└── README.md
```

## Coding Guidelines

### Code Style
- Follow standard Java conventions
- Use meaningful variable names
- Add JavaDoc comments for public methods
- Keep methods focused and small

### Example
```java
/**
 * Calculates the overlap percentage between two ways.
 * 
 * @param newWay The newly drawn way
 * @param oldWay The existing OSM way
 * @return Percentage of oldWay covered by newWay (0.0 to 1.0)
 */
private double calculateOverlapPercentage(Way newWay, Way oldWay) {
    double oldArea = Geometry.computeArea(oldWay);
    if (oldArea == 0) {
        return 0;
    }
    double intersectionArea = Geometry.getAreaOfIntersection(newWay, oldWay);
    return intersectionArea / oldArea;
}
```

### JOSM API Usage
- Prefer JOSM's built-in utilities
- Use Command pattern for all data modifications
- Follow JOSM plugin best practices
- Reference: https://josm.openstreetmap.de/wiki/DevelopersGuide

### Error Handling
- Catch specific exceptions
- Provide user-friendly error messages
- Log errors for debugging
- Don't crash JOSM!

```java
try {
    performMerge(dataSet);
} catch (IllegalArgumentException e) {
    new Notification("Invalid data: " + e.getMessage())
        .setIcon(JOptionPane.ERROR_MESSAGE)
        .show();
} catch (Exception e) {
    e.printStackTrace();
    new Notification("Merge failed. Check console for details.")
        .setIcon(JOptionPane.ERROR_MESSAGE)
        .show();
}
```

## Pull Request Process

### 1. Fork and Branch
```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
```

### 2. Make Changes
- Write clean, documented code
- Follow coding guidelines
- Test your changes thoroughly

### 3. Test
- Manual testing in JOSM
- Verify no regressions
- Test edge cases
- Run build: `./gradlew build`

### 4. Commit
Use conventional commit messages:
```
feat: add configurable overlap threshold
fix: resolve conflict detection bug
docs: update installation guide
test: add unit tests for overlap calculation
```

### 5. Push and Create PR
```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub with:
- Clear title
- Description of changes
- Related issue number (if applicable)
- Testing performed

### 6. Code Review
- Respond to feedback
- Make requested changes
- Keep discussion focused and respectful

## Testing Checklist

Before submitting a PR:
- [ ] Code builds without errors
- [ ] Plugin loads in JOSM
- [ ] Filter auto-activation works
- [ ] Merge & Fix works correctly
- [ ] No console errors
- [ ] Documentation updated (if needed)
- [ ] No breaking changes (or clearly documented)

## Feature Development Guidelines

### Adding a New Feature

1. **Discuss First**
   - Open a GitHub Issue
   - Describe the feature
   - Get feedback from maintainers

2. **Design**
   - Plan the implementation
   - Consider edge cases
   - Think about user experience

3. **Implement**
   - Follow coding guidelines
   - Add appropriate error handling
   - Update documentation

4. **Test**
   - Test the happy path
   - Test edge cases
   - Test integration with existing features

5. **Document**
   - Update README.md
   - Update USER_GUIDE.md
   - Add to TECHNICAL.md if needed

### Example: Adding Configurable Threshold

```java
// 1. Add preference setting
public class DPWMapperPlugin extends Plugin {
    public static final String PREF_OVERLAP_THRESHOLD = "dpwmapper.overlap_threshold";
    
    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new DPWMapperPreferenceSetting();
    }
}

// 2. Create preference panel
public class DPWMapperPreferenceSetting implements PreferenceSetting {
    // UI for configuring threshold
}

// 3. Use preference in algorithm
double threshold = Main.pref.getDouble(
    DPWMapperPlugin.PREF_OVERLAP_THRESHOLD, 
    0.50 // default
);
```

## Bug Fix Guidelines

### Finding Bugs
- Check GitHub Issues
- Test the plugin yourself
- Review user reports

### Fixing Bugs

1. **Reproduce**
   - Confirm the bug exists
   - Identify steps to reproduce
   - Understand the root cause

2. **Fix**
   - Make minimal changes
   - Don't introduce new features
   - Add error handling if needed

3. **Test**
   - Verify the bug is fixed
   - Test related functionality
   - Ensure no regressions

4. **Document**
   - Update CHANGELOG
   - Reference issue in commit
   - Add comments if complex

## Release Process

### Version Numbering
Follow Semantic Versioning (SemVer):
- **1.0.0** - Major version (breaking changes)
- **1.1.0** - Minor version (new features, backwards compatible)
- **1.0.1** - Patch version (bug fixes)

### Creating a Release

1. Update version in `build.gradle`
2. Update version in `dpwmapper.properties`
3. Update CHANGELOG.md
4. Commit: `git commit -m "chore: bump version to 1.1.0"`
5. Tag: `git tag -a v1.1.0 -m "Release version 1.1.0"`
6. Push: `git push && git push --tags`
7. GitHub Actions will build and create release

## Communication

### Be Respectful
- Respectful and constructive feedback
- Assume good intentions
- Help newcomers

### Be Clear
- Write clear issue descriptions
- Provide context in PRs
- Ask questions if unclear

### Be Patient
- Maintainers are volunteers
- Reviews take time
- Not all features can be accepted

## Resources

### JOSM Development
- Developer Guide: https://josm.openstreetmap.de/wiki/DevelopersGuide
- Plugin Development: https://josm.openstreetmap.de/wiki/DevelopersGuide/DevelopingPlugins
- API Documentation: https://josm.openstreetmap.de/doc/

### Related Projects
- utilsplugin2: https://github.com/JOSM/utilsplugin2
- JOSM: https://github.com/JOSM/josm

### OpenStreetMap
- OSM Wiki: https://wiki.openstreetmap.org/
- Tagging Guidelines: https://wiki.openstreetmap.org/wiki/Map_Features
- HOT: https://www.hotosm.org/

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

## Questions?

- Open a GitHub Issue
- Check existing documentation
- Ask in the PR/Issue comments

Thank you for contributing! 🎉
